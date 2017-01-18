package wbs.integrations.dialogue.api;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("dialogueResponder")
public
class DialogueResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		FormatWriter formatWriter =
			requestContext.formatWriter ();

		formatWriter.writeLineFormat (
			"<HTML>");

		formatWriter.writeLineFormat (
			"<!-- X-E3-Submission-Report: \"00\" -->");

		formatWriter.writeLineFormat (
			"</HTML>");

	}

}
