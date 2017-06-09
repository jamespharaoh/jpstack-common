package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivClose;
import static wbs.web.utils.HtmlBlockUtils.htmlDivOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.encodeNewlineToBr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;

@PrototypeComponent ("imChatPendingSummaryPart")
public
class ImChatPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	@NamedDependency ("imChatPendingSummaryCustomerFormType")
	ConsoleFormType <ImChatCustomerRec> customerFormType;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("imChatPendingSummaryMessageFormType")
	ConsoleFormType <ImChatMessageRec> messageFormType;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	@NamedDependency ("imChatPendingSummaryProfileFormType")
	ConsoleFormType <ImChatProfileRec> profileFormType;

	// state

	ConsoleForm <ImChatCustomerRec> customerForm;
	ConsoleForm <ImChatProfileRec> profileForm;
	ConsoleForm <ImChatMessageRec> messageForm;

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatProfileRec profile;
	ImChatRec imChat;

	boolean canSupervise;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			JqueryScriptRef.instance,
			JqueryEditableScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/im-chat.js")

		);

	}

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/im-chat.css")

		);

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// load data

			message =
				imChatMessageHelper.findFromContextRequired (
					transaction);

			conversation =
				message.getImChatConversation ();

			customer =
				conversation.getImChatCustomer ();

			profile =
				conversation.getImChatProfile ();

			imChat =
				customer.getImChat ();

			// setup forms

			customerForm =
				customerFormType.buildResponse (
					transaction,
					emptyMap (),
					customer);

			profileForm =
				profileFormType.buildResponse (
					transaction,
					emptyMap (),
					profile);

			messageForm =
				messageFormType.buildResponse (
					transaction,
					emptyMap (),
					message);

			// misc

			canSupervise =
				privChecker.canRecursive (
					transaction,
					imChat,
					"supervisor");

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlDivOpen (
				formatWriter,
				htmlClassAttribute (
					"layout-container"));

			htmlTableOpen (
				formatWriter,
				htmlClassAttribute (
					"layout"));

			htmlTableRowOpen (
				formatWriter);

			htmlTableCellOpen (
				formatWriter,
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"width",
						"50%")));

			goCustomerSummary (
				transaction,
				formatWriter);

			goCustomerDetails (
				transaction,
				formatWriter);

			htmlTableCellClose (
				formatWriter);

			htmlTableCellOpen (
				formatWriter,
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"width",
						"50%")));

			goProfileSummary (
				transaction,
				formatWriter);

			goCustomerNotes (
				transaction,
				formatWriter);

			htmlTableCellClose (
				formatWriter);

			htmlTableRowClose (
				formatWriter);

			htmlTableClose (
				formatWriter);

			htmlDivClose (
				formatWriter);

			goHistory (
				transaction,
				formatWriter);

		}

	}

	void goCustomerDetails (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		htmlHeadingThreeWrite (
			formatWriter,
			"Customer details");

		htmlTableOpenDetails (
			formatWriter);

		for (
			ImChatCustomerDetailTypeRec detailType
				: imChat.getCustomerDetailTypes ()
		) {

			if (

				detailType.getRestricted ()

				&& ! canSupervise

			) {
				continue;
			}

			ImChatCustomerDetailValueRec detailValue =
				customer.getDetails ().get (
					detailType.getId ());

			htmlTableDetailsRowWrite (
				formatWriter,
				detailType.getName (),
				ifNotNullThenElseEmDash (
					detailValue,
					() -> detailValue.getValue ()));

		}

		htmlTableClose (
			formatWriter);

	}

	void goCustomerSummary (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goCustomerSummary");

		) {

			htmlHeadingThreeWrite (
				formatWriter,
				"Customer summary");

			customerForm.outputDetailsTable (
				transaction,
				formatWriter);

		}

	}

	private
	void goProfileSummary (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goProfileSummary");

		) {

			htmlHeadingThreeWrite (
				formatWriter,
				"Profile summary");

			profileForm.outputDetailsTable (
				transaction,
				formatWriter);

		}

	}

	private
	void goCustomerNotes (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goCustomerNotes");

		) {

			htmlHeadingThreeWrite (
				formatWriter,
				"Notes");

			htmlParagraphWriteHtml (
				formatWriter,
				encodeNewlineToBr (
					customer.getNotesText () != null
						? customer.getNotesText ().getText ()
						: ""),
				htmlIdAttribute (
					stringFormat (
						"im-chat-customer-note-%s",
						integerToDecimalString (
							customer.getId ()))),
				htmlClassAttribute (
					"im-chat-customer-note-editable"));


		}
	}

	private
	void goHistory (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHistory");

		) {

			htmlHeadingThreeWrite (
				formatWriter,
				"Conversation history");

			// retrieve messages

			List <ImChatMessageRec> messages =
				new ArrayList<> (
					conversation.getMessagesIn ());

			List <ImChatMessageRec> historyRequests =
				Lists.reverse (
					messages);

			// create message table

			htmlTableOpenList (
				formatWriter);

			// header

			htmlTableRowOpen (
				formatWriter);

			messageForm.outputTableHeadings (
				transaction,
				formatWriter);

			htmlTableRowClose (
				formatWriter);

			// row

			for (
				ImChatMessageRec historyRequest
					: historyRequests
			) {

				if (
					isNotNull (
						historyRequest.getPartnerImChatMessage ())
				) {

					ImChatMessageRec historyReply =
						historyRequest.getPartnerImChatMessage ();

					htmlTableRowOpen (
						formatWriter,
						htmlClassAttribute (
							classForMessage (
								historyReply)));

					messageForm.outputTableCellsList (
						transaction,
						formatWriter,
						historyReply,
						true);

					htmlTableRowClose (
						formatWriter);

				}

				htmlTableRowOpen (
					formatWriter,
					htmlClassAttribute (
						presentInstances (

					Optional.of (
						classForMessage (
							historyRequest)),

					optionalIf (
						referenceEqualWithClass (
							ImChatMessageRec.class,
							message,
							historyRequest),
						() -> "selected")

				)));

				messageForm.outputTableCellsList (
					transaction,
					formatWriter,
					historyRequest,
					true);

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

	String classForMessage (
			@NonNull ImChatMessageRec message) {

		if (
			isNotNull (
				message.getPrice ())
		) {

			return "message-out-charge";

		} else if (
			isNotNull (
				message.getSenderUser ())
		) {

			return "message-out";

		} else {

			return "message-in";

		}

	}

}
