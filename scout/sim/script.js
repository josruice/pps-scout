function undraw()
{
	var canvas = document.getElementById("canvas");
	var ctx = canvas.getContext("2d");
	ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function draw_grid(min_x, min_y, max_x, max_y, rows, cols)
{
	var canvas = document.getElementById("canvas");
	var ctx = canvas.getContext("2d");
	if (min_x < 0 || max_x > canvas.width)
		throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
	if (min_y < 0 || max_y > canvas.height)
		throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
    // draw vertical lines
    for (var col = 0 ; col <= cols ; ++col) {
        ctx.beginPath();
        ctx.moveTo(min_x + col * (max_x - min_x) / cols, min_y);
        ctx.lineTo(min_x + col * (max_x - min_x) / cols, max_y);
        ctx.closePath();
        ctx.lineWidth = 2;
        ctx.strokeStyle = "grey";
        ctx.stroke();
    }
    // draw horizontal lines
    for (var row = 0 ; row <= rows ; ++row) {
        ctx.beginPath();
        ctx.moveTo(min_x, min_y + row * (max_y - min_y) / rows);
        ctx.lineTo(max_x, min_y + row * (max_y - min_y) / rows);
        ctx.closePath();
        ctx.lineWidth = 2;
        ctx.strokeStyle = "grey";
        ctx.stroke();
    }
}

function rand(n) {
    return Math.floor((Math.random() * n));
}

Number.prototype.hashCode = function(){
    return (this*2654435761) % 4294967296;
}

function draw_dots(min_x, min_y, max_x, max_y, rows, cols, num, xcoords, ycoords, color, IDs, isenemy) {
    var ctx = document.getElementById('canvas').getContext('2d');
    for(var i = 0 ; i < num ; ++ i) {
        ctx.beginPath();
        //ctx.moveTo(min_x + xcoords[i] * (max_x - min_x) / cols,
        //    min_y + ycoords[i] * (max_y - min_y) / rows );
        var locationx = min_x + xcoords[i] * (max_x - min_x) / cols + (max_x - min_x) / (4*cols);
        var locationy = min_y + ycoords[i] * (max_y - min_y) / rows+ (max_y - min_y) / (4*rows);
        if(!isenemy) {
            locationx +=  IDs[i].hashCode()%((max_x - min_x) / (2*cols));
            locationy +=  IDs[i].hashCode()%((max_y - min_y) / (2*rows))
        }
        var radius = 4;
        if(rows > 25) radius = 3;
        if(rows > 50) radius = 2;
        ctx.arc(
            locationx, 
            locationy, 
            radius, 0, 2 * Math.PI);
        ctx.fillStyle = color;
        ctx.fill();
        if(IDs != null) {
            ctx.font = "14px Arial";
            ctx.textAlign = "left";
            ctx.lineWidth = 1;
            ctx.strokeStyle = "black";
            ctx.strokeText(IDs[i],        locationx + 1, locationy - 1);
        }
    }
}

function draw_landmarks(min_x, min_y, max_x, max_y, rows, cols, num, xcoords, ycoords, color) {
    var ctx = document.getElementById('canvas').getContext('2d');
    var width = (max_x - min_x) / (cols);
    var height = (max_y - min_y) / rows;
    for(var i = 0 ; i < num ; ++ i) {
        var offset_x = min_x + width*xcoords[i];
        var offset_y = min_y + height*ycoords[i];
        ctx.beginPath();
        ctx.moveTo(offset_x + width/2, offset_y + height/4);
        ctx.lineTo(offset_x + width/4, offset_y + 3*height/4);
        ctx.lineTo(offset_x + 3*width/4, offset_y + 3* height/4);
        ctx.closePath();
        ctx.lineWidth = 4;
        ctx.strokeStyle = "black";
        ctx.stroke();
        ctx.fillStyle=color;
        ctx.fill();
    }
}

function draw_outpost(min_x, min_y, max_x, max_y, rows, cols) {
    var ctx = document.getElementById('canvas').getContext('2d');
    var width = (max_x - min_x) / (cols);
    var height = (max_y - min_y) / rows;
    ctx.beginPath();
    offset_x = min_x;
    offset_y = min_y;
    ctx.moveTo(offset_x + width/4, offset_y + height/4);
    ctx.lineTo(offset_x + width/2, offset_y);
    ctx.lineTo(offset_x + 3*width/4, offset_y + height/4);
    ctx.lineTo(offset_x + 3*width/4, offset_y + 3*height/4);
    ctx.lineTo(offset_x + width/4, offset_y + 3*height/4);
    ctx.closePath();
    ctx.lineWidth = 4;
    ctx.strokeStyle = "black";
    ctx.stroke();
    ctx.fillStyle="yellow";
    ctx.fill();


    ctx.beginPath();
    offset_x = max_x - width;
    offset_y = min_y;
    ctx.moveTo(offset_x + width/4, offset_y + height/4);
    ctx.lineTo(offset_x + width/2, offset_y);
    ctx.lineTo(offset_x + 3*width/4, offset_y + height/4);
    ctx.lineTo(offset_x + 3*width/4, offset_y + 3*height/4);
    ctx.lineTo(offset_x + width/4, offset_y + 3*height/4);
    ctx.closePath();
    ctx.lineWidth = 4;
    ctx.strokeStyle = "black";
    ctx.stroke();
    ctx.fillStyle="yellow";
    ctx.fill();


    ctx.beginPath();
    offset_x = min_x;
    offset_y = max_y - height;
    ctx.moveTo(offset_x + width/4, offset_y + height/4);
    ctx.lineTo(offset_x + width/2, offset_y);
    ctx.lineTo(offset_x + 3*width/4, offset_y + height/4);
    ctx.lineTo(offset_x + 3*width/4, offset_y + 3*height/4);
    ctx.lineTo(offset_x + width/4, offset_y + 3*height/4);
    ctx.closePath();
    ctx.lineWidth = 4;
    ctx.strokeStyle = "black";
    ctx.stroke();
    ctx.fillStyle="yellow";
    ctx.fill();


    ctx.beginPath();
    offset_x = max_x - width;
    offset_y = max_y - height;
    ctx.moveTo(offset_x + width/4, offset_y + height/4);
    ctx.lineTo(offset_x + width/2, offset_y);
    ctx.lineTo(offset_x + 3*width/4, offset_y + height/4);
    ctx.lineTo(offset_x + 3*width/4, offset_y + 3*height/4);
    ctx.lineTo(offset_x + width/4, offset_y + 3*height/4);
    ctx.closePath();
    ctx.lineWidth = 4;
    ctx.strokeStyle = "black";
    ctx.stroke();
    ctx.fillStyle="yellow";
    ctx.fill();
}

function draw_side(min_x, min_y, max_x, max_y, group, turns, colors, score)
{
	var canvas = document.getElementById("canvas");
	var ctx = canvas.getContext("2d");
	if (min_x < 0 || max_x > canvas.width)
		throw "Invalid x-axis bounds: " + min_x + " - " + max_x;
	if (min_y < 0 || max_y > canvas.height)
		throw "Invalid y-axis bounds: " + min_y + " - " + max_y;
    // draw message
    ctx.font = "32px Arial";
    ctx.textAlign = "left";
    ctx.lineWidth = 4;
    ctx.strokeStyle = "darkgrey";
    ctx.strokeText("Player: " + group,        min_x, min_y + 30);
    ctx.strokeText("Turns left: " + turns,         min_x, min_y + 60);
    ctx.strokeText("Score: " + score,         min_x, min_y + 90);
    // ctx.strokeText("CPU time: " + cpu + " s", min_x, min_y + 90);
    // ctx.strokeText("Legend:", min_x, min_y + 150);
    ctx.fillStyle = "darkblue";
    ctx.fillText("Player: " + group,        min_x, min_y + 30);
    ctx.fillText("Turns left: " + turns,         min_x, min_y + 60);
    ctx.fillText("Score: " + score,         min_x, min_y + 90);
}


function process(data)
{
    // parse data
    data = data.split(",");
    var group = data[0];
    var n = Number(data[1]);
    var turns_left = Number(data[2]);
    var refresh = Number(data[3]);
    var s = Number(data[4]);
    var scoutx = new Array(s);
    var scouty = new Array(s);
    for(var i = 0; i < s ; ++i) {
        scoutx[i] = Number(data[5 + 2 * i]);
        scouty[i] = Number(data[6 + 2 * i]);
    }
    var e = Number(data[5 + 2*s]);
    var enemyx = new Array(e);
    var enemyy = new Array(e);
    for(var i = 0; i < e ; ++i) {
        enemyx[i] = Number(data[6 + 2*s + 2 * i]);
        enemyy[i] = Number(data[7 + 2*s + 2 * i]);
    }

    var landmarkCount = Number(data[6 + 2*s + 2*e]);
    var landmarkx = new Array(landmarkCount);
    var landmarky = new Array(landmarkCount);
    for(var i = 0; i < landmarkCount ; ++i) {
        landmarkx[i] = Number(data[7 + 2*s + 2*e  +2 * i]);
        landmarky[i] = Number(data[8 + 2*s + 2*e  +2 * i]);
    }

    var scoutIDs = new Array(s);

    for(var i = 0 ; i < s ; ++ i) {
        scoutIDs[i] = Number(data[7 + 2*s + 2*e + 2*landmarkCount + i]);
    }
    var score = Number(data[7 + 2*s + 2*e + 2*landmarkCount + s]);
    console.log("data", data);
    if (refresh < 0.0) refresh = -1;
    else refresh = Math.round(refresh);

    // draw grid
    undraw();
    draw_grid(300, 50, 900, 650, n+2, n+2, "black");
    // draw for 1st player
    var colors = ["orange", "black", "purple", "green", "blue"];
    draw_outpost(300, 50, 900, 650, n+2, n+2);
    console.log("count: " + landmarkCount);
    draw_landmarks(300, 50, 900, 650, n+2, n+2, landmarkCount, landmarkx, landmarky, "limegreen")
    draw_dots(300, 50, 900, 650, n+2, n+2, e, enemyx, enemyy, "red", null, true);
    draw_dots(300, 50, 900, 650, n+2, n+2, s, scoutx, scouty, "blue", scoutIDs, false);
    draw_side ( 10,  40,  190, 690, group, turns_left, colors, score);
    //draw_outpost(250, 50, 850, 650 , n+2, n+2);
    //draw_shape(250,  50,  850, 650, 50, 50, buildings, cuts, colors, types, highlight == 0);
    return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout)
{
	var xhr = new XMLHttpRequest();
	xhr.onload = (function() {
		var refresh = -1;
		try {
			if (xhr.readyState != 4)
				throw "Incomplete HTTP request: " + xhr.readyState;
			if (xhr.status != 200)
				throw "Invalid HTTP status: " + xhr.status;
			refresh = process(xhr.responseText);
			if (latest_version < version && paused == 0)
				latest_version = version;
			else
				refresh = -1;
		} catch (message) { alert(message); }
		if (refresh >= 0)
			setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
	});
	xhr.onabort   = (function() { location.reload(true); });
	xhr.onerror   = (function() { location.reload(true); });
	xhr.ontimeout = (function() {
		if (version <= latest_version)
			console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
		else if (retries == 0)
			location.reload(true);
		else {
			console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
			ajax(version, retries - 1, timeout * 2);
		}
	});
	xhr.open("GET", "data.txt", true);
	xhr.responseType = "text";
	xhr.timeout = timeout;
	xhr.send();
}

function pause() {
    paused = (paused + 1) % 2;
}

var paused = 0;
ajax(0, 10, 100);
