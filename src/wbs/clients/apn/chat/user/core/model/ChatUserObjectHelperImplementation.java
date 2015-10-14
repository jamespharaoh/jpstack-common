package wbs.clients.apn.chat.user.core.model;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatNumberReportLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.framework.utils.RandomLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

@Log4j
public
class ChatUserObjectHelperImplementation
	implements ChatUserObjectHelperMethods {

	// dependencies

	@Inject
	RandomLogic randomLogic;

	// indirect dependencies

	@Inject
	Provider<ChatNumberReportLogic> chatNumberReportLogicProvider;

	@Inject
	Provider<ChatUserObjectHelper> chatUserHelperProvider;

	@Inject
	Provider<ChatUserLogic> chatUserLogicProvider;

	@Inject
	Provider<NumberLogic> numberLogicProvider;

	// implementation

	@Override
	public
	ChatUserRec findOrCreate (
			ChatRec chat,
			MessageRec message) {

		// resolve dependencies

		ChatNumberReportLogic chatNumberReportLogic =
			chatNumberReportLogicProvider.get ();

		ChatUserObjectHelper chatUserHelper =
			chatUserHelperProvider.get ();

		NumberLogic numberLogic =
			numberLogicProvider.get ();

		// resolve stuff

		NumberRec number =
			message.getNumber ();

		// check for an existing ChatUser

		ChatUserRec chatUser =
			chatUserHelper.find (
				chat,
				number);

		if (chatUser != null) {

			// check number

			if (
				! chatNumberReportLogic.isNumberReportSuccessful (number)
				&& number.getArchiveDate () == null
			) {

				log.debug (
					stringFormat (
						"Number archiving %s code %s",
						number.getNumber (),
						chatUser.getCode ()));

				NumberRec newNumber =
					numberLogic.archiveNumberFromMessage (
						message);

				return create (
					chat,
					newNumber);

			}

			return chatUser;

		}

		return create (
			chat,
			number);

	}

	@Override
	public
	ChatUserRec findOrCreate (
			ChatRec chat,
			NumberRec number) {

		// resolve dependencies

		ChatUserObjectHelper chatUserHelper =
			chatUserHelperProvider.get ();

		// check for an existing ChatUser

		ChatUserRec chatUser =
			chatUserHelper.find (
				chat,
				number);

		if (chatUser != null)
			return chatUser;

		return create (
			chat,
			number);

	}

	@Override
	public
	ChatUserRec create (
			ChatRec chat,
			NumberRec number) {

		ChatUserObjectHelper chatUserHelper =
			chatUserHelperProvider.get ();

		ChatUserLogic chatUserLogic =
			chatUserLogicProvider.get ();

		// create him

		ChatUserRec chatUser =
			new ChatUserRec ()

			.setChat (
				chat)

			.setNumber (
				number)

			.setOldNumber (
				number)

			.setType (
				ChatUserType.user)

			.setCode (
				randomLogic.generateNumericNoZero (6))

			.setDeliveryMethod (
				ChatMessageMethod.sms)

			.setGender (
				chat.getGender ())

			.setOrient (
				chat.getOrient ())

			.setCreditMode (
				number.getFree ()
					? ChatUserCreditMode.free
					: ChatUserCreditMode.strict);

		chatUserLogic.monitorCap (
			chatUser);

		// set adult verify on some services
		// TODO this should probably not be here

		if (chat.getAutoAdultVerify ()) {

			chatUserLogic.adultVerify (
				chatUser);

		}

		chatUserHelper.insert (
			chatUser);

		return chatUser;

	}

}