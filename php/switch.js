$(function () {

	var cookieName = "txt2_console";
	var cookieDuration = 365;

	var idCounter = 0;

	var consoleValues = [
		{
			name: "current",
			description: [
				"Current version, this is the version you should normally ",
				"use, unless you are told otherwise or having problems caused ",
				"by new changes.",
			].join (""),
		},
		{
			name: "previous",
			description: [
				"Previous version, use this if you have trouble with new ",
				"features which have been released to current.",
			].join (""),
		},
		{
			name: "next",
			description: [
				"Next version, use this if you have been asked to test the ",
				"new version before it is made current.",
			].join (""),
		},
		{
			name: "test",
			description: [
				"Test version for very new and/or incomplete features. Only ",
				"use this if you are asked specifically to.",
			].join (""),
		},
		{
			name: "dev",
			description: [
				"Development version, which will regularly be very broken. ",
				"Only use this if you are asked specifically to.",
			].join (""),
		},
	];

	var consoleSelected =
		Cookies.get (cookieName) || "live";

	function main () {
		consoleSetup ();
	}

	function consoleSetup () {

		var consoleParagraph =
			$("p.console");

		consoleParagraph.empty ();

		consoleValues.forEach (function (consoleValue) {

			var thisId =
				idCounter ++;

			var consoleOption =
				$("<span>");

			var consoleInput = (
				$("<input>")
					.attr ("id", "id-" + thisId)
					.attr ("type", "radio")
					.attr ("name", "console")
					.attr ("value", consoleValue.name)
			);

			if (consoleSelected == consoleValue.name) {

				consoleInput
					.prop ("checked", true);

			}

			consoleOption.append (
				consoleInput);

			consoleOption.append (
				$("<label>")
					.css ("font-weight", "bold")
					.attr ("for", "id-" + thisId)
					.text (consoleValue.name)
			);

			consoleOption.append (
				$("<p>")
					.css ("margin-left", "1em")
					.text (consoleValue.description)
			);

			consoleParagraph.append (
				consoleOption);

			consoleInput.change (
				consoleUpdate);

		});

	}

	function consoleUpdate () {

		consoleSelected =
			$(this).val ();

		Cookies.set (
			cookieName,
			consoleSelected,
			{ expires: cookieDuration });

	}

	main ();

});
