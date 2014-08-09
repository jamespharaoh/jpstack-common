package wbs.sms.magicnumber.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("magicNumberCreatePart")
public
class MagicNumberCreatePart
	extends AbstractPagePart {

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p>Numbers<br>\n",
			"<textarea",
			" name=\"numbers\"",
			" rows=\"16\"",
			" cols=\"40\"",
			">%h</textarea></p>\n",
			requestContext.parameter ("numbers"));

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"create magic numbers\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}