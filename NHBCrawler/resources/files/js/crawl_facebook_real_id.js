"use strict";

var system = require('system');

var ARGS = (function(systemArgs) {
	var ACCEPTED_ARGS = [ "--email", "--pass", "--app-scope-ids", "--casper-path", "--cli" ];
	var args = {};
	for (var i = 0; i < systemArgs.length; i++) {
		var element = systemArgs[i];
		if (element) {
			element = element.trim();
			for (var j = 0; j < ACCEPTED_ARGS.length; j++) {
				var argName = ACCEPTED_ARGS[j].trim();
				if (element.indexOf(argName) == 0) {
					element = element.substring(argName.length, element.length);
					if (element.indexOf("=") == 0) {
						element = element.substring(1, element.length);
					}
					if (argName.indexOf("--") == 0) {
						argName = argName.substring(2);
					}
					element = element.trim();
					if (argName == "app-scope-ids") {
						args[argName] = JSON.parse(element);
					} else {
						args[argName] = element.length == 0 ? true : element;
					}
				}
			}
		}
	}
	return args;
})(system.args);

var OUTPUT = {
	accountBlocked : false,
	success : {},
	fail : {}
}

var facebookUrl = "https://www.facebook.com/";

var casper = require('casper').create({
	pageSettings : {
		loadImages : false,
		loadPlugins : false,
		userAgent : "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/602.4.8 (KHTML, like Gecko) Version/10.0.3 Safari/602.4.8"
	},
	viewportSize : {
		width : 981,
		height : 500
	}
});

var capture = (function(_casper, outputFolder) {
	function getFormattedDate() {
		var date = new Date();
		return [ date.getFullYear(), date.getMonth() + 1, date.getDate() ].join("-") + " "
				+ [ date.getHours(), date.getMinutes(), date.getSeconds() ].join(":") + "." + date.getMilliseconds();
	}
	var id = 1;
	var subFolder = getFormattedDate() + " (" + ARGS.email + ")";
	var folder = outputFolder + "/" + subFolder + "/";
	return function(imagePrefix) {
		var fileName = folder + (id++) + "." + (imagePrefix ? imagePrefix : "captured") + " (" + getFormattedDate() + ").png";
		_casper.capture(fileName);
	}
})(casper, "output/screenshots");

// Opens facebook homepage
casper.start(facebookUrl);

casper.then(function() {
	capture("before-login");
	console.log("Login with email: " + ARGS["email"]);
	this.evaluate(function(email, pass) {
		document.querySelector("input[name=email]").value = email;
		document.querySelector("input[name=pass]").value = pass;
		document.getElementById("login_form").submit();
	}, ARGS["email"], ARGS["pass"]);
});

casper.then(function() {
	this.wait(1000);
});

casper.then(function() {
	capture("after-login");
});

casper.then(function() {
	function next() {
		if (list.length == 0) {
			return;
		}
		var id = list.shift();
		var nextLink = facebookUrl + id;
		console.log("Friend timeline url: " + nextLink);
		casper.thenOpen(nextLink, function() {
			var url = this.getCurrentUrl().trim();
			console.log("---> Url after redirect: " + url);
			if (nextLink == url) {
				capture("redirect-fail-" + id);
				OUTPUT.fail[id] = {
					timelineUrl : url
				}
			} else {
				var realId = undefined;
				var regexs = [ /id=(\d+)/ig, /\/([A-Za-z0-9\.]+)$/ig ];
				for (var i = 0; i < regexs.length; i++) {
					var regex = regexs[i];
					var matched = regex.exec(url);
					if (matched) {
						console.log("    |_ " + regex + " -> matched: " + JSON.stringify(matched));
						realId = matched[1];
						break;
					}
				}
				if (!realId || realId == id) {
					capture("crawl-fail-" + id);
					OUTPUT.fail[id] = {
						timelineUrl : url
					}
				} else {
					OUTPUT.success[id] = {
						realId : realId,
						timelineUrl : url
					};
				}
				// this.waitForSelector("a.profilePicThumb", function() {
				// capture("timeline-openned");
				// var anchor = document.querySelector("a.profilePicThumb");
				// console.log("Href: " + (anchor ? anchor.href : null));
				// });
			}
			casper.wait(20, next);
		});
	}

	var loggedInUrl = this.getCurrentUrl().trim();
	if (loggedInUrl.indexOf("/checkpoint/?next=https") >= 0) {
		OUTPUT.accountBlocked = true;
	} else {
		var list = ARGS["app-scope-ids"];
		next();
	}
});

casper.then(function() {
	console.log(JSON.stringify(OUTPUT));
});

casper.run();
