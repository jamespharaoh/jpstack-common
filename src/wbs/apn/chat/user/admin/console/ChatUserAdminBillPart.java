package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.time.TimeUtils.millisToInstant;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.bill.console.ChatUserBillLogConsoleHelper;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatUserAdminBillPart")
public
class ChatUserAdminBillPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatUserBillLogConsoleHelper chatUserBillLogHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;
	ChatRec chat;

	List <ChatUserBillLogRec> todayBillLogs;
	List <ChatUserBillLogRec> allBillLogs;
	boolean billLimitReached;

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

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			if (
				enumEqualSafe (
					chatUser.getType (),
					ChatUserType.monitor)
			) {
				return;
			}

			DateTimeZone timezone =
				chatUserLogic.getTimezone (
					chatUser);

			LocalDate today =
				transaction
					.now ()
					.toDateTime (timezone)
					.toLocalDate ();

			Instant startTime =
				today
					.toDateTimeAtStartOfDay (timezone)
					.toInstant ();

			Instant endTime =
				today
					.plusDays (1)
					.toDateTimeAtStartOfDay (timezone)
					.toInstant ();

			todayBillLogs =
				chatUserBillLogHelper.findByTimestamp (
					transaction,
					chatUser,
					new Interval (
						startTime,
						endTime));

			allBillLogs =
				chatUserBillLogHelper.findByTimestamp (
					transaction,
					chatUser,
					new Interval (
						millisToInstant (0),
						endTime));

			billLimitReached =
				chatCreditLogic.userBillLimitApplies (
					transaction,
					chatUser);

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

			if (
				enumEqualSafe (
					chatUser.getType (),
					ChatUserType.monitor)
			) {

				formatWriter.writeLineFormat (
					"<p>This is a monitor and cannot be billed.</p>");

				return;

			}

			boolean dailyAdminRebillLimitReached =
				todayBillLogs.size () >= 3;

			boolean canBypassDailyAdminRebillLimit =
				requestContext.canContext (
					"chat.manage");

			if (billLimitReached) {

				formatWriter.writeFormat (
					"<p>Daily billed message limit reached.</p>");

			}

			if (dailyAdminRebillLimitReached) {

				formatWriter.writeLineFormat (
					"<p>Daily admin rebill limit reached<br>");

				formatWriter.writeLineFormat (
					"%h admin rebills have been actioned today</p>",
					integerToDecimalString (
						todayBillLogs.size ()));

			}

			if (
				! billLimitReached
				&& (
					! dailyAdminRebillLimitReached
					|| canBypassDailyAdminRebillLimit
				)
			) {

				htmlFormOpenPostAction (
					formatWriter,
					requestContext.resolveLocalUrl (
						"/chatUser.admin.bill"));

				formatWriter.writeFormat (
					"<p><input",
					" type=\"submit\"",
					" value=\"reset billing\"",
					"></p>");

				htmlFormClose (
					formatWriter);

			}

			htmlHeadingTwoWrite (
				formatWriter,
				"History");

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Date",
				"Time",
				"User");

			for (
				ChatUserBillLogRec billLog
					: allBillLogs
			) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.dateStringShort (
						chatUserLogic.getTimezone (
							chatUser),
						billLog.getTimestamp ()));

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timeString (
						chatUserLogic.getTimezone (
							chatUser),
						billLog.getTimestamp ()));

				consoleObjectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					billLog.getUser ());

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}