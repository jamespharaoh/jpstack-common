package wbs.apn.chat.contact.console;

import static wbs.sms.gsm.GsmUtils.gsmStringLength;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.gsm.GsmUtils;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.model.ChatBlockObjectHelper;
import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatMonitorInboxFormAction")
public
class ChatMonitorInboxFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatBlockObjectHelper chatBlockHelper;

	@SingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	ChatContactNoteConsoleHelper chatContactNoteHelper;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatMonitorInboxFormResponder")
	Provider <WebResponder> formResponderProvider;

	@PrototypeDependency
	@NamedDependency ("queueHomeResponder")
	Provider <WebResponder> queueHomeResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return formResponderProvider.get ();

	}

	// implementation

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// get stuff

			Long monitorInboxId =
				requestContext.stuffIntegerRequired (
					"chatMonitorInboxId");

			// get params

			String text =
				requestContext.parameterRequired (
					"text");

			boolean ignore =
				optionalIsPresent (
					requestContext.parameter (
						"ignore"));

			boolean note =
				optionalIsPresent (
					requestContext.parameter (
						"sendAndNote"));

			// check params

			if (! ignore) {

				if (text.length () == 0) {

					requestContext.addError (
						"Please enter a message");

					return null;

				}

				if (! GsmUtils.gsmStringIsValid (text)) {

					requestContext.addError (
						"Message text is invalid");

					return null;

				}

				if (
					moreThan (
						gsmStringLength (
							text),
						ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
							* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES)
				) {

					requestContext.addError (
						"Message text is too long");

					return null;

				}

			}

			// get database objects

			ChatMonitorInboxRec chatMonitorInbox =
				chatMonitorInboxHelper.findRequired (
					transaction,
					monitorInboxId);

			ChatUserRec monitorChatUser =
				chatMonitorInbox.getMonitorChatUser ();

			ChatUserRec userChatUser =
				chatMonitorInbox.getUserChatUser ();

			ChatRec chat =
				userChatUser.getChat ();

			if (ignore) {

				// check if they can ignore

				if (
					! privChecker.canRecursive (
						transaction,
						chat,
						"manage")
				) {

					requestContext.addError (
						"Can't ignore");

					return null;

				}

			} else {

				if (
					lessThan (
						GsmUtils.gsmStringLength (text),
						chat.getMinMonitorMessageLength ())
				) {

					requestContext.addError (
						stringFormat (
							"Message text is too short (minimum %s)",
							integerToDecimalString (
								chat.getMinMonitorMessageLength ())));

					return null;

				}

				ChatBlockRec chatBlock =
					chatBlockHelper.find (
						transaction,
						userChatUser,
						monitorChatUser);

				boolean blocked =
					chatBlock != null
					|| userChatUser.getBlockAll ();

				boolean deleted =
					chatUserLogic.deleted (
						transaction,
						userChatUser);

				// create a chat message

				TextRec textRec =
					textHelper.findOrCreate (
						transaction,
						text);

				ChatMessageRec chatMessage =
					chatMessageHelper.insert (
						transaction,
						chatMessageHelper.createInstance ()

					.setChat (
						chat)

					.setFromUser (
						monitorChatUser)

					.setToUser (
						userChatUser)

					.setTimestamp (
						transaction.now ())

					.setOriginalText (
						textRec)

					.setEditedText (
						blocked
							? null
							: textRec)

					.setStatus (
						blocked
							? ChatMessageStatus.blocked
							: ChatMessageStatus.sent)

					.setSender (
						userConsoleLogic.userRequired (
							transaction))

				);

				// update contact entry, set monitor warning if first message

				ChatContactRec chatContact =
					chatContactHelper.findOrCreate (
						transaction,
						monitorChatUser,
						userChatUser);

				if (
					chatContact.getLastDeliveredMessageTime () == null
					&& ! monitorChatUser.getStealthMonitor ()
				) {

					chatMessage

						.setMonitorWarning (
							true);

				}

				chatContact

					.setLastDeliveredMessageTime (
						transaction.now ());

				// update chat user stats

				if (! (blocked || deleted)) {

					userChatUser

						.setLastReceive (
							transaction.now ());

				}

				// send message

				if (! (blocked || deleted)) {

					chatMessageLogic.chatMessageDeliverToUser (
						transaction,
						chatMessage);

				}

				// charge

				if (! (blocked || deleted)) {

					chatCreditLogic.userReceiveSpend (
						transaction,
						userChatUser,
						1l);

				}

				// update monitor last action

				monitorChatUser

					.setLastAction (
						transaction.now ());

				// create a note

				if (note) {

					chatContactNoteHelper.insert (
						transaction,
						chatContactNoteHelper.createInstance ()

						.setUser (
							userChatUser)

						.setMonitor (
							monitorChatUser)

						.setNotes (
							text)

						.setTimestamp (
							transaction.now ())

						.setConsoleUser (
							userConsoleLogic.userRequired (
								transaction))

						.setChat (
							userChatUser.getChat ())

					);

				}

			}

			// and delete the monitor inbox entry

			queueLogic.processQueueItem (
				transaction,
				chatMonitorInbox.getQueueItem (),
				userConsoleLogic.userRequired (
					transaction));

			chatMonitorInboxHelper.remove (
				transaction,
				chatMonitorInbox);

			// commit transaction

			transaction.commit ();

			// add notice

			if (ignore) {

				requestContext.addNotice (
					"Message ignored");

			} else if (note) {

				requestContext.addNotice (
					"Message sent and note added");

			} else {

				requestContext.addNotice (
					"Message sent");

			}

			// and return

			return queueHomeResponderProvider.get ();

		}

	}

}