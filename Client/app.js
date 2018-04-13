//Copyright 2017, Avery Vine, All rights reserved.

var express = require('express');
var app = express();
var request = require('request');

const ROOT = "./public";
const baseURL = "http://localhost:8080/WingIt";
const urlPrefix = "/url/?url=";
const adminPrefix = "/admin/?adminRequest=";

//receive a port, or select default port
app.set('port', (process.env.PORT || 5000));

//log each server request
app.use(function(req, res, next) {
	console.log(req.method + " request for " + req.url);
	next();
});

//handles all user requests
app.get(['/', '/index.html', '/index'], function(req, res) {
	if (req.query.url === undefined) {
		res.sendFile('index.html', { root: ROOT });
	} else {
		let targetURL = req.query.url;
		console.log("Request url: " + baseURL + urlPrefix + targetURL);
		request(baseURL + urlPrefix + targetURL, function(error, response, body) {
			console.log("Received data for url: " + body);
			res.setHeader('Content-Type', 'application/json');
			res.send(JSON.stringify(body));
		});
	}
});

//handles all admin requests
app.get(['/admin.html', '/admin'], function(req, res) {
	if (req.query.adminRequest === undefined) {
		res.sendFile('admin.html', { root: ROOT });
	} else {
		let adminRequest = req.query.adminRequest;
		console.log("Request url: " + baseURL + adminPrefix + adminRequest);
		request(baseURL + adminPrefix + adminRequest, function(error, response, body) {
			console.log("Received data for url: " + body);
			res.setHeader('Content-Type', 'application/json');
			res.send(JSON.stringify(body));
		});
	}
});

//send all other static files
app.use(express.static(ROOT));

//send 404 for anything other request
app.all("*", function(req, res) {
    res.status(404);
	res.sendFile('404.html', { root: ROOT });
})

//start listening on the selected port
app.listen(app.get('port'), function() {
    console.log('Server listening on port', app.get('port'));
});