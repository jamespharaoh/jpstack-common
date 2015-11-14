package wbs.clients.apn.chat.core.daemon;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.keyword.model.ChatKeywordObjectHelper;
import wbs.clients.apn.chat.keyword.model.ChatKeywordRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectManager;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.CommandManager;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatMagicCommand")
public
class ChatMagicCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatKeywordObjectHelper chatKeywordHelper;

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandManager commandManager;

	@Inject
	Database database;

	@Inject
	KeywordFinder keywordFinder;

	@Inject
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.magic"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				command);

		// look for a single keyword

		for (
			KeywordFinder.Match match
				: keywordFinder.find (rest)
		) {

			if (! rest.isEmpty ())
				continue;

			ChatKeywordRec chatKeyword =
				chatKeywordHelper.findByCode (
					chat,
					match.simpleKeyword ());

			if (
				chatKeyword != null
				&& chatKeyword.getGlobal ()
				&& chatKeyword.getCommand () != null
			) {

				return commandManager.handle (
					inbox,
					chatKeyword.getCommand (),
					Optional.<Integer>absent (),
					"");

			}

		}

		// use the default command

		CommandRec defaultCommand =
			commandHelper.find (
				commandRef.get ());

		return commandManager.handle (
			inbox,
			defaultCommand,
			Optional.<Integer>absent (),
			rest);

	}

}