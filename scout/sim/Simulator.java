package scout.sim;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Simulator {
  private static String group, landmark_mapper_name, enemy_mapper_name;
  private static String root = "scout";
  private static long seed;
  private static int n, t, s, e, fps;
  private static long gui_refresh;
  private static boolean gui_enabled, log;
  private static int repeats = 1;
  private static long play_timeout = 1000;
  private static long init_timeout = 1000;

  public static void main(String[] args) throws Exception {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if(compiler == null) throw new IOException(":(");
    n = t = s = e = -1;
    group = landmark_mapper_name = enemy_mapper_name = null;
    parseArgs(args);
    if(n == -1 || t == -1 || s == -1 || e == -1 || group == null ||
            landmark_mapper_name == null || enemy_mapper_name == null) {
      throw new IllegalArgumentException("Missing arguments");
    }

    Class<Player> player_class = loadPlayer(group);
    Class<LandmarkMapper> landmark_mapper_class = loadLandmarkMapper(landmark_mapper_name);
    Class<EnemyMapper> enemy_mapper_class = loadEnemyMapper(enemy_mapper_name);
    Timer timer = new Timer();
    timer.start();
    int total_score = 0;
    for(int r = 0 ; r < repeats; ++ r) {
      seed = System.currentTimeMillis();
      Player[] scouts = new Player[s];
      for(int i = 0 ; i < s; ++ i) {
        scouts[i] = player_class.getDeclaredConstructor(int.class).newInstance(i);
      }
      LandmarkMapper landmarkMapper = landmark_mapper_class.newInstance();
      EnemyMapper enemyMapper = enemy_mapper_class.newInstance();
      int score = new Simulator().play(n, t, s, e, timer, scouts, landmarkMapper, enemyMapper, seed);
      if(repeats == 1)
        System.out.println("score: " + score);
      total_score += score;
    }
    if(repeats != 1)
      System.out.println("Average score: "+total_score*1.0/repeats);
    System.exit(0);
  }

  private int play(int n, int t, int s, int e, Timer timer, Player[] scouts, LandmarkMapper landmarkMapper, EnemyMapper enemyMapper, Long seed) throws Exception {
    HTTPServer server = null;
    

    if (gui_enabled) {
      server = new HTTPServer();
      if (!Desktop.isDesktopSupported())
        System.err.println("Desktop operations not supported");
      else if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        System.err.println("Desktop browse operation not supported");
      else {
        try {
          Desktop.getDesktop().browse(new URI("http://localhost:" + server.port()));
        } catch (URISyntaxException exc) {
          exc.printStackTrace();
        }
      }
    }

    Grid grid = new Grid(n);
    //add scouts randomly
    grid.addAllCellObjects(Arrays.asList(scouts), new ScoutMapper().getLocations(n, s, new Random(seed)));

    //add landmarks according to landmarkMapper
    int landmarkCount = landmarkMapper.getCount(n);
    List<CellObject> landmarks = new ArrayList<>();
    List<Point> landmarkLocations = landmarkMapper.getLocations(n);
    for(int i = 0 ; i < landmarkCount; ++i ) {
      landmarks.add(new Landmark(i, landmarkLocations.get(i).x, landmarkLocations.get(i).y));
    }
    if (landmarkCount != landmarkLocations.size()) throw new Exception("landmark mapper count not right");

    grid.addAllCellObjects(landmarks, landmarkLocations);

    //add enemies according to enemymapper
    List<Point> enemyLocations = new ArrayList<>(
      enemyMapper.getLocations(n, e, landmarkLocations, new Random(seed + 1)));
    if (enemyLocations.size() != e) throw new Exception("enemy mapper count not right");
    List<CellObject> enemies = new ArrayList<>();
    Random enemyIDGen = new Random(seed + 2);
    Set<Integer> enemyIDSet = new HashSet<>();
    for(int i = 0 ; i < e; ++i) {
      Integer enemyID = enemyIDGen.nextInt(1000000);
      while(enemyIDSet.contains(enemyID)) {
        enemyID = enemyIDGen.nextInt(1000000);
      }
      enemyIDSet.add(enemyID);
      enemies.add(new Enemy(enemyID));
    }
    grid.addAllCellObjects(enemies, enemyLocations);

    //add outposts
    List<CellObject> outposts = new ArrayList<>();
    List<Point> outpostLocations = new ArrayList<>();
    outpostLocations.add(new Point(0,0));
    outpostLocations.add(new Point(n+1,0));
    outpostLocations.add(new Point(n+1,n+1));
    outpostLocations.add(new Point(0,n+1));
    for(int i = 0 ; i < 4; ++i) {
      outposts.add(new Outpost(i, n, outpostLocations.get(i).x, outpostLocations.get(i).y));
    }
    grid.addAllCellObjects(outposts, outpostLocations);

    //init
    for(Player scout: scouts) {
      try {
        final int ss=s, nn=n, tt = t;
        timer.call(new Callable<Void>() {
          public Void call() throws Exception {
            scout.init(scout.getID(), ss, nn, tt, landmarkLocations);
            return null;
          }
        }, init_timeout);
      } catch (Exception ex) {
        System.err.println("Exception calling init of player: " + scout.getID());
        ex.printStackTrace();
      }
      
    }


    Map<String, Point> nextLocation = new HashMap<>();
    Map<String, Integer> turnsToWait = new HashMap<>();
    for(Player scout : scouts) {
      turnsToWait.put(scout.getID(), -1);
    }
    while(t > 0) {
      if(t%100 == 1)
      if (log) System.out.println("turns left: " + t);

      boolean[] communicated = new boolean[s];
      for(Player scout : scouts) {
        if(turnsToWait.containsKey(scout.getID())) {

          ArrayList<ArrayList<ArrayList<String>>> nearbyIDs = new ArrayList<>();
          for(int i = 0 ; i < 3; ++i) {
            ArrayList<ArrayList<String>> row = new ArrayList<>();
            for(int j = 0 ; j < 3 ; ++ j) {
              row.add(new ArrayList<>());
            }
            nearbyIDs.add(row);
          }
          Point currentLocation = grid.location.get(scout.getID());
          int[] x = {-1,0,1,-1,0,1,-1,0,1};
          int[] y = {-1,-1,-1,0,0,0,1,1,1};
          for(int i = 0 ; i < 9 ; ++ i) {
            Point nbr = grid.getLocationWithOffset(currentLocation, new Point(x[i], y[i]));
            if(nbr != null && grid.getCell(nbr) != null) {
              for(CellObject obj : grid.getCell(nbr)) {
                nearbyIDs.get(1 + x[i]).get(1 + y[i]).add(obj.getID());
              }
            } else {
              nearbyIDs.get(1 + x[i]).set(1 + y[i], null);
            }
          }
          try {
            timer.call(new Callable<Void>() {
              public Void call() throws Exception {
                scout.communicate(nearbyIDs, grid.getCell(currentLocation));
                return null;
              }
            }, play_timeout);
          } catch (Exception ex) {
            System.err.println("Exception calling communicate of player: " + scout.getID());
            ex.printStackTrace();
          }

          int turns = turnsToWait.get(scout.getID());
          if(turns == 0) {
            grid.update(scout, nextLocation.get(scout.getID()));
            //turnsToWait.put(scout.getID(), turns);
          } else if(turns > 0 ){
            //turnsToWait.put(scout.getID(), turns);
          } else {
          }
        }
      }

      List<Point> scoutLocations = new ArrayList<>();
      for(Player scout: scouts) {
        scoutLocations.add(grid.location.get(scout.getID()));
        if (turnsToWait.containsKey(scout.getID())) {
          int turns = turnsToWait.get(scout.getID());
          if(turns >= 0) {
            -- turns;
            turnsToWait.put(scout.getID(), turns);
          } else
          if (turns < 0) {

            ArrayList<ArrayList<ArrayList<String>>> nearbyIDs = new ArrayList<>();
            for(int i = 0 ; i < 3; ++i) {
              ArrayList<ArrayList<String>> row = new ArrayList<>();
              for(int j = 0 ; j < 3 ; ++ j) {
                row.add(new ArrayList<>());
              }
              nearbyIDs.add(row);
            }
            Point currentLocation = grid.location.get(scout.getID());
            int[] x = {-1,0,1,-1,0,1,-1,0,1};
            int[] y = {-1,-1,-1,0,0,0,1,1,1};
            for(int i = 0 ; i < 9 ; ++ i) {
              Point nbr = grid.getLocationWithOffset(currentLocation, new Point(x[i], y[i]));
              if(nbr != null && grid.getCell(nbr) != null) {
                for(CellObject obj : grid.getCell(nbr)) {
                  nearbyIDs.get(1 + x[i]).get(1 + y[i]).add(obj.getID());
                }
              } else {
                nearbyIDs.get(1 + x[i]).set(1 + y[i], null);
              }
            }
            //System.out.println("actual: " + currentLocation.x + ", " + currentLocation.y);
            // for(int i = 0; i < 3; ++i) {
            //   for(int j = 0 ; j < 3; ++j) {
            //     System.out.print(nearbyIDs.get(i).get(j) == null? "0":"1");
            //   }
            //   System.out.println();
            // }
            // System.out.println();
            Point direction = null;
            try {
              direction = timer.call(new Callable<Point>() {
                public Point call() throws Exception {
                  return scout.move(nearbyIDs, grid.getCell(currentLocation));
                }
              }, play_timeout);
            } catch (Exception ex) {
              System.err.println("Exception calling move of player: " + scout.getID());
              ex.printStackTrace();
            }
            if(direction == null) {
              direction = new Point(0,0);
            }

            if(!(direction.x <= 1 && direction.x >= -1 && direction.y >=-1 && direction.y <= 1)) throw new Exception("move returned illegal value");
            Point next = grid.getLocationWithOffset(
                    currentLocation,
                    direction
            );
            if(next == null) {
              System.err.println("trying to move to invalid location");
              continue;
            }
            nextLocation.put(scout.getID(), next);

            boolean isNearEnemy = false;
            for(CellObject obj : grid.getCell(currentLocation)) {
              if (obj instanceof Enemy) {
                isNearEnemy  = true;
              }
            }
            for(CellObject obj : grid.getCell(next)) {
              if (obj instanceof Enemy) {
                isNearEnemy  = true;
              }
            }
            boolean isDiag = false;
            if(direction.x != 0 && direction.y != 0) isDiag = true;
            turns = 2;
            if(isDiag) turns  = 3;
            if(isNearEnemy) turns *= 3;
            turns -= 2;
            turnsToWait.put(scout.getID(), turns);
          }
        }
      }

      --t;
      if(gui_enabled)
        gui(
          server, 
          state(
            group, 
            n, 
            t, 
            scouts,
            scoutLocations,
            enemyLocations,
            landmarkLocations,
            gui_refresh
          )
        );
    }

    int score = 0;

    if(log) {
      System.out.println("Enemy Map (X is enemy, 0 is not):");
      for(int i = 0 ; i <= n + 1 ; ++ i) {
        for (int j = 0; j <=n + 1; ++j) {
          List<CellObject> objs = grid.getCell(i, j);
          boolean hasEnemy = false;
          for (CellObject obj : objs) {
            if (obj instanceof Enemy) {
              hasEnemy = true;
            }
          }
          if(hasEnemy)
            System.out.print("X");
          else
            System.out.print("0");
        }
        System.out.println();
      }
      System.out.println();
    }

    if (log) {
      System.out.println("Player Map (player: 1, not player: 0");
      for(int i = 0 ; i <= n + 1 ; ++ i) {
        for (int j = 0; j <=n + 1; ++j) {
          List<CellObject> objs = grid.getCell(i, j);
          boolean hasEnemy = false;
          for (CellObject obj : objs) {
            if (obj instanceof Player) {
              hasEnemy = true;
            }
          }
          if(hasEnemy)
            System.out.print("1");
          else
            System.out.print("0");
        }
        System.out.println();
      }
      System.out.println();
    }

    if(log) {
      System.out.println("Landmark Map, Landmark: ], Not landmark: 0");
      for(int i = 0 ; i <= n + 1 ; ++ i) {
        for (int j = 0; j <=n + 1; ++j) {
          List<CellObject> objs = grid.getCell(i, j);
          boolean hasEnemy = false;
          for (CellObject obj : objs) {
            if (obj instanceof Landmark) {
              hasEnemy = true;
            }
          }
          if(hasEnemy)
            System.out.print("]");
          else
            System.out.print("0");
        }
        System.out.println();
      }
      System.out.println();
    }

    if(log) {
      System.out.println("Outpost information(X: Enemy, -: Safe, 0: unknown:");

      for(CellObject _outpostobj : outposts) {
        List<List<Integer>> outpostMap = ((Outpost) _outpostobj).getEnemyMap();
        for(int i = 0 ; i <= n + 1 ; ++ i) {
          for (int j = 0; j <=n + 1; ++j) {
            int sss = outpostMap.get(i).get(j);
            String xxx;
            if(sss == 1) xxx = "X";
            else if(sss == 2) xxx = "-";
            else xxx = "0";
            System.out.print(xxx);
          }
          System.out.println();
        }
        System.out.println();
      }
    }


    int[] enemies_discovered = new int[4];
    int[] safe_discovered = new int[4];
    int[] mistakes = new int[4];
    int enemies_missed = 0;
    for(int i = 1 ; i < n + 1 ; ++ i) {
      for (int j = 1; j < n + 1; ++j) {
        List<CellObject> objs = grid.getCell(i, j);
        boolean hasEnemy = false;
        for (CellObject obj : objs) {
          if (obj instanceof Enemy) {
            hasEnemy = true;
          }
        }
        if(hasEnemy) {
        //  System.out.println(i + ", " + j +" has enemy");
        }
        boolean foundEnemy = false;
        for (int o = 0; o < 4; ++ o) {
          CellObject _outpostobj = outposts.get(o);
          List<List<Integer>> outpostMap = ((Outpost) _outpostobj).getEnemyMap();
          if(outpostMap.get(i).get(j)!=-1)
         // System.out.println(i + ", " + j + " of " + _outpostobj.getID() + ": " + outpostMap.get(i).get(j));
          if (outpostMap.get(i).get(j) == 1) {
            foundEnemy = true;
            if(hasEnemy) {
              score += 1000;
              enemies_discovered[o]++;
            }
            else {
              score -= 1000;
              mistakes[o]++;
              if(log) {
                System.out.println("location: (" + i +", " +j + ") was incorrect");
              }
            }
          } else if(outpostMap.get(i).get(j) == 2) {
            if(hasEnemy) {
              mistakes[o]++;
              System.out.println("location: (" + i +", " +j + ") was incorrect");
              score -= 1000;
            } else {
              safe_discovered[o]++;
              score += 1;
            }
          }
        }
        if(!foundEnemy && hasEnemy) {
          enemies_missed ++ ;
          score -= 5000;
        }
      }
    }
    if(log) {
      for(int i = 0 ; i < 4; ++ i) {
        System.out.println("Enemies found by outpost "+i+": " + enemies_discovered[i]);
        System.out.println("Safe locations found by outpost "+i+": " + safe_discovered[i]);
        System.out.println("Mistakes by outpost "+i+": " + mistakes[i]);
        
      }
      System.out.println("Enemies missed: " + enemies_missed);
    }
    if(server != null) server.close();
    return score;
  }

  private static void parseArgs(String[] args) {
    for(int i = 0; i < args.length; ++i) {

      if (args[i].equals("-p") || args[i].equals("--player")) {
        if (i + 1 >= args.length) {
          throw new IllegalArgumentException("Missing player name");
        }
        group = args[++i];
      } else if (args[i].equals("-m") || args[i].equals("--map")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing map name");
        }
        landmark_mapper_name = args[++i];
      } else if (args[i].equals("-em") || args[i].equals("--emap")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing enemy map name");
        }
        enemy_mapper_name = args[++i];
      } else if (args[i].equals("-s") || args[i].equals("--scouts")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing number of scouts");
        }
        s = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-n") || args[i].equals("--board")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing board size");
        }
        n = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-t") || args[i].equals("--time")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing time");
        }
        t = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-e") || args[i].equals("--enemies")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing enemies");
        }
        e = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-r") || args[i].equals("--repeats")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing repeats");
        }
        repeats = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-S") || args[i].equals("--seed")) {
        if (i + 1 >= args.length) {
          throw new IllegalArgumentException("Missing seed");
        }
        seed = Long.parseLong(args[++i]);
      }else if (args[i].equals("-f") || args[i].equals("--fps")) {
        if (i+1 >= args.length) {
          throw new IllegalArgumentException("Missing fps");
        }
        double gui_fps = Double.parseDouble(args[++i]);
        gui_refresh = gui_fps > 0.0 ? (long) Math.round(1000.0 / gui_fps) : -1;
        gui_enabled = true;
      } else if (args[i].equals("--gui")) {
        gui_enabled = true;
      } else if (args[i].equals("--verbose")) {
        log = true;
      } else {
        throw new IllegalArgumentException("Unknown argument: " + args[i]);
      }
    }
  }

  private static long last_modified(Iterable <File> files) {
    long last_date = 0;
    for (File file : files) {
      long date = file.lastModified();
      if (last_date < date)
        last_date = date;
    }
    return last_date;
  }

  private static Class <Player> loadPlayer(String group) throws IOException, ReflectiveOperationException {
    String sep = File.separator;
    Set<File> player_files = directory(root + sep + group, ".java");
    File class_file = new File(root + sep + group + sep + "Player.class");
    long class_modified = class_file.exists() ? class_file.lastModified() : -1;
    if (class_modified < 0 || class_modified < last_modified(player_files) ||
            class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      System.out.println(System.getProperty( "java.home"));
      if (compiler == null)
        throw new IOException("Cannot find Java compiler");
      StandardJavaFileManager manager = compiler.
              getStandardFileManager(null, null, null);
      long files = player_files.size();
      if (log)
        System.err.print("Compiling " + files + " .java files ... ");
      if (!compiler.getTask(null, manager, null, null, null,
              manager.getJavaFileObjectsFromFiles(player_files)).call())
        throw new IOException("Compilation failed");
      System.err.println("done!");
      class_file = new File(root + sep + group + sep + "Player.class");
      if (!class_file.exists())
        throw new FileNotFoundException("Missing class file");
    }
    ClassLoader loader = Simulator.class.getClassLoader();
    if (loader == null)
      throw new IOException("Cannot find Java class loader");
    @SuppressWarnings("rawtypes")
    Class raw_class = loader.loadClass(root + "." + group + ".Player");
    @SuppressWarnings("unchecked")
    Class <Player> player_class = raw_class;
    return player_class;
  }

  private static Class <EnemyMapper> loadEnemyMapper(String mapper) throws IOException, ReflectiveOperationException {
    String sep = File.separator;
    Set <File> sequencer_files = directory(root + sep + mapper, ".java");
    File class_file = new File(root + sep + mapper + sep + "EnemyMapper.class");
    long class_modified = class_file.exists() ? class_file.lastModified() : -1;
    if (class_modified < 0 || class_modified < last_modified(sequencer_files) ||
            class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null)
        throw new IOException("Cannot find Java compiler");
      StandardJavaFileManager manager = compiler.
              getStandardFileManager(null, null, null);
      long files = sequencer_files.size();
      if (log)
        System.err.print("Compiling " + files + " .java files ... ");
      if (!compiler.getTask(null, manager, null, null, null,
              manager.getJavaFileObjectsFromFiles(sequencer_files)).call())
        throw new IOException("Compilation failed");
      if (log)
        System.err.println("done!");
      class_file = new File(root + sep + mapper + sep + "EnemyMapper.class");
      if (!class_file.exists())
        throw new FileNotFoundException("Missing class file");
    }
    ClassLoader loader = Simulator.class.getClassLoader();
    if (loader == null)
      throw new IOException("Cannot find Java class loader");
    @SuppressWarnings("rawtypes")
    Class raw_class = loader.loadClass(root + "." + mapper + ".EnemyMapper");
    @SuppressWarnings("unchecked")
    Class <EnemyMapper> mapper_class = raw_class;
    return mapper_class;
  }

  private static Class <LandmarkMapper> loadLandmarkMapper(String mapper) throws IOException, ReflectiveOperationException {
    String sep = File.separator;
    Set <File> sequencer_files = directory(root + sep + mapper, ".java");
    File class_file = new File(root + sep + mapper + sep + "LandmarkMapper.class");
    long class_modified = class_file.exists() ? class_file.lastModified() : -1;
    if (class_modified < 0 || class_modified < last_modified(sequencer_files) ||
            class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null)
        throw new IOException("Cannot find Java compiler");
      StandardJavaFileManager manager = compiler.
              getStandardFileManager(null, null, null);
      long files = sequencer_files.size();
      if (log)
        System.err.print("Compiling " + files + " .java files ... ");
      if (!compiler.getTask(null, manager, null, null, null,
              manager.getJavaFileObjectsFromFiles(sequencer_files)).call())
        throw new IOException("Compilation failed");
      if (log)
        System.err.println("done!");
      class_file = new File(root + sep + mapper + sep + "LandmarkMapper.class");
      if (!class_file.exists())
        throw new FileNotFoundException("Missing class file");
    }
    ClassLoader loader = Simulator.class.getClassLoader();
    if (loader == null)
      throw new IOException("Cannot find Java class loader");
    @SuppressWarnings("rawtypes")
    Class raw_class = loader.loadClass(root + "." + mapper + ".LandmarkMapper");
    @SuppressWarnings("unchecked")
    Class <LandmarkMapper> mapper_class = raw_class;
    return mapper_class;
  }

  private static Set <File> directory(String path, String extension) {
    Set <File> files = new HashSet <File> ();
    Set <File> prev_dirs = new HashSet <File> ();
    prev_dirs.add(new File(path));
    do {
      Set <File> next_dirs = new HashSet<File>();
      for (File dir : prev_dirs)
        for (File file : dir.listFiles())
          if (!file.canRead()) ;
          else if (file.isDirectory())
            next_dirs.add(file);
          else if (file.getPath().endsWith(extension))
            files.add(file);
      prev_dirs = next_dirs;
    } while (!prev_dirs.isEmpty());
    return files;
  }

  public static String state(
    String group, 
    int n, 
    int turns_left, 
    Player[] scouts,
    List<Point> scoutLocations,
    List<Point> enemyLocations,
    List<Point> landmarkLocations,
    long gui_refresh) {

    String buffer = "";
    buffer += group + ",";
    buffer += n + ",";
    buffer += turns_left + ",";
    buffer += gui_refresh + ",";
  
    buffer += scoutLocations.size() + ",";
    for(Point p : scoutLocations) {
      buffer += p.y + ",";
      buffer += p.x + ",";
    }
    
    buffer += enemyLocations.size() + ",";
    for(Point p : enemyLocations) {
      buffer += p.y + ",";
      buffer += p.x + ",";
    }

    buffer += landmarkLocations.size() + ",";
    for(Point p : landmarkLocations) {
      buffer += p.y + ",";
      buffer += p.x + ",";
    }

    for(Player scout : scouts) {
      buffer += scout.getID().substring(1) + ",";
    }
    return buffer;
  }

  public static void gui(HTTPServer server, String content) {
    String path = null;
    for (;;) {
        // get request
      for (;;)
      try {
        path = server.request();
        break;
      } catch (IOException e) {
        System.err.println("HTTP request error: " + e.getMessage());
      }
        // dynamic content
      if (path.equals("data.txt")) {
        // send dynamic content
        try {
          server.reply(content);
          return;
        } catch (IOException e) {
          System.err.println("HTTP dynamic reply error: " + e.getMessage());
          continue;
        }
      }
      // static content
      if (path.equals("")) path = "webpage.html";
      else if (!path.equals("favicon.ico") &&
           !path.equals("apple-touch-icon.png") &&
           !path.equals("script.js")) break;
      // send file
      File file = new File(root + File.separator + "sim"
           + File.separator + path);
      try {
        server.reply(file);
      } catch (IOException e) {
        System.err.println("HTTP static reply error: " + e.getMessage());
      }
    }
  }
}