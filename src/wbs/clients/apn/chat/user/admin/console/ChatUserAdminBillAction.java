package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.bill.model.ChatUserBillLogObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserAdminBillAction")
public
class ChatUserAdminBillAction
	extends ConsoleAction {

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatUserBillLogObjectHelper chatUserBillLogHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminBillResponder");
	}

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext ("chat.userCredit")) {
			requestContext.addError ("Access denied");
			return null;
		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// lock prevents race condition between limit check and update

		chatUserHelper.lock (
			chatUser);

		// enforce £30/day limit

		if (chatCreditLogic.userBillLimitApplies (chatUser)) {

			requestContext.addError ("Daily limit reached");

			return null;

		}

		// enforce three per day limit

		LocalDate today =
			LocalDate.now ();

		Instant startOfToday =
			today
				.toDateTimeAtStartOfDay ()
				.toInstant ();

		Instant endOfToday =
			today
				.plusDays (1)
				.toDateTimeAtStartOfDay ()
				.toInstant ();

		List<ChatUserBillLogRec> todayBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					startOfToday,
					endOfToday));

		boolean dailyAdminBillLimitReached =
			todayBillLogs.size () > 3;

		boolean canBypassDailyAdminBillLimit =
			requestContext.canContext ("chat.manage");

		if (
			dailyAdminBillLimitReached
			&& ! canBypassDailyAdminBillLimit
		) {

			requestContext.addError (
				"Daily admin bill limit reached");

		}

		// bill the user

		chatCreditLogic.userBillReal (
			chatUser,
			true);

		// log it

		chatUserBillLogHelper.insert (
			new ChatUserBillLogRec ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setUser (
				myUser)

		);

		transaction.commit ();

		requestContext.addNotice (
			"User billed");

		return null;

	}

}