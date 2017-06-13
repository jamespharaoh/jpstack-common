package wbs.test.fixtures;

import lombok.extern.log4j.Log4j;

@Log4j
@SuppressWarnings ("unused")
public
class TestFixtures {

	// singleton dependencies

/*
	@SingletonDependency
	AutoResponderObjectHelper autoResponderHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CommandLogic commandLogic;

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordObjectHelper keywordHelper;

	@SingletonDependency
	KeywordSetObjectHelper keywordSetHelper;

	@SingletonDependency
	ManualResponderObjectHelper manualResponderHelper;

	@SingletonDependency
	ManualResponderTemplateObjectHelper manualResponderTemplateHelper;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	PrivObjectHelper privHelper;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	UserPrivObjectHelper userPrivHelper;
*/

	/*
		// existing items

		RootRec root =
			rootHelper.find (0);

		SliceRec testSlice =
			sliceHelper.findByCode (
				GlobalId.root,
				"test");

		List<UserRec> users =
			userHelper.findAll ();

		SenderRec simulatorSender =
			senderHelper.findByCode (
				GlobalId.root,
				"simulator");

		NetworkRec unknownNetwork =
			networkHelper.findByCode (
				GlobalId.root,
				"unknown");

		CurrencyRec gbpCurrency =
			currencyHelper.findByCode (
				GlobalId.root,
				"gbp");

		// routes

		RouteRec billRoute =
			objectManager.insert (
				new RouteRec ()
					.setSlice (testSlice)
					.setCode ("chat_5_00")
					.setName ("Chat £5.00")
					.setDescription ("Chat billed £1")
					.setNumber ("c500")
					.setOutCharge (500)
					.setCanSend (true)
					.setCanReceive (true)
					.setDeliveryReports (true)
					.setSender (simulatorSender));

		RouteRec freeRoute =
			objectManager.insert (
				new RouteRec ()
					.setSlice (testSlice)
					.setCode ("chat_free")
					.setName ("Chat free")
					.setDescription ("Chat free")
					.setCanSend (true)
					.setDeliveryReports (true)
					.setSender (simulatorSender));

		RouterRec freeRouter =
			objectManager.findChildByCode (
				RouterRec.class,
				freeRoute,
				"static");

		CommandRec magicCommand =
			commandHelper.findByCode (root, "magic_number");

		RouteRec magicRoute =
			objectManager.insert (
				new RouteRec ()
					.setSlice (testSlice)
					.setCode ("chat_magic")
					.setName ("Chat magic")
					.setDescription ("Chat magic")
					.setCanReceive (true)
					.setCommand (magicCommand));

		// chat

		ChatRec chat =
			objectManager.insert (
				new ChatRec ()
					.setSlice (testSlice)
					.setCode ("test")
					.setName ("Test")
					.setDescription ("Test")
					.setCurrency (gbpCurrency));

		// templates

		ChatHelpTemplateRec spendWarning1Template =
			objectManager.insert (
				new ChatHelpTemplateRec ()
					.setChat (chat)
					.setType ("system")
					.setCode ("spend_warning_1")
					.setText ("spend 1"));

		ChatHelpTemplateRec spendWarning2Template =
			objectManager.insert (
				new ChatHelpTemplateRec ()
					.setChat (chat)
					.setType ("system")
					.setCode ("spend_warning_2")
					.setText ("spend 2"));

		// chat schemes

		ChatSchemeRec leftChatScheme =
			objectManager.insert (
				new ChatSchemeRec ()
					.setChat (chat)
					.setCode ("left")
					.setName ("Left")
					.setDescription ("Left")
					.setRbBillRoute (billRoute)
					.setRbFreeRouter (freeRouter)
					.setMagicRouter (freeRouter)
					.setWapRouter (freeRouter));

		ChatSchemeRec rightChatScheme =
			objectManager.insert (
				new ChatSchemeRec ()
					.setChat (chat)
					.setCode ("right")
					.setName ("Right")
					.setDescription ("Right")
					.setRbBillRoute (billRoute)
					.setRbFreeRouter (freeRouter)
					.setMagicRouter (freeRouter)
					.setWapRouter (freeRouter));

		// chat users, regular

		List<ChatUserRec> chatUsers =
			new ArrayList<ChatUserRec> ();

		for (int index = 0; index < 260; index ++) {

			String code =
				String.format (
					"%06d",
					index + 100000);

			String numberString =
				String.format (
					"447979%s",
					code);

			NumberRec number =
				objectManager.insert (
					new NumberRec ()
						.setNumber (numberString)
						.setNetwork (unknownNetwork));

			ChatUserRec chatUser =
				objectManager.insert (
					new ChatUserRec ()
						.setChat (chat)
						.setChatScheme (leftChatScheme)
						.setCode (code)
						.setType (ChatUserType.user)
						.setNumber (number));

			chatUsers.add (chatUser);

		}

		// chat users, monitors

		List<ChatUserRec> chatMonitors =
			new ArrayList<ChatUserRec> ();

		for (
			int index = 0;
			index < 260;
			index ++
		) {

			String code =
				String.format (
					"%06d",
					index + 200000);

			ChatUserRec chatUser =
				chatUserHelper.insert (
					new ChatUserRec ()
						.setChat (chat)
						.setCode (code)
						.setType (ChatUserType.monitor));

			chatMonitors.add (chatUser);

		}

		database.flush ();

		// chat messages, between users

		log.info (
			"Creating chat messages between users");

		for (
			int index = 0;
			index < 1000;
			index ++
		) {

			TextRec messageText =
				textHelper.findOrCreate (
					stringFormat (
						"Chat message user to user %s",
						index));

			chatMessageHelper.insert (
				new ChatMessageRec ()

				.setChat (
					chat)

				.setFromUser (
					pickOne (chatUsers, random))

				.setToUser (
					pickOne (chatUsers, random))

				.setTimestamp (
					pastInstant (now, random).toDate ())

				.setSource (
					ChatMessageMethod.api)

				.setMethod (
					ChatMessageMethod.api)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			if (index % 100 == 0)
				database.flush ();

		}

		database.flush ();

		// chat messages, to monitors

		log.info (
			"Creating chat messages to monitors");

		for (
			int index = 0;
			index < 1000;
			index ++
		) {

			TextRec messageText =
				textHelper.findOrCreate (
					stringFormat (
						"Chat message to monitor %s",
						index));

			chatMessageHelper.insert (
				new ChatMessageRec ()

				.setChat (
					chat)

				.setFromUser (
					pickOne (chatUsers, random))

				.setToUser (
					pickOne (chatMonitors, random))

				.setTimestamp (
					pastInstant (now, random).toDate ())

				.setMethod (
					ChatMessageMethod.api)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			if (index % 100 == 0)
				database.flush ();

		}

		database.flush ();

		// chat messages, from monitors

		log.info (
			"Creating chat messages from monitors");

		for (
			int index = 0;
			index < 1000;
			index ++
		) {

			TextRec messageText =
				textHelper.findOrCreate (
					stringFormat (
						"Chat message from monitor %s",
						index));

			chatMessageHelper.insert (
				new ChatMessageRec ()

				.setChat (
					chat)

				.setFromUser (
					pickOne (chatMonitors, random))

				.setToUser (
					pickOne (chatUsers, random))

				.setTimestamp (
					pastInstant (now, random).toDate ())

				.setSender (
					pickOne (users, random))

				.setMethod (
					ChatMessageMethod.api)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			if (index % 100 == 0)
				database.flush ();

		}

		database.flush ();

	}

	public
	void createAutoResponderFixtures () {

		log.info (
			"Creating auto responder fixtures");

		// existing items

		SliceRec testSlice =
			sliceHelper.findByCode (
				GlobalId.root,
				"test");

		KeywordSetRec inboundKeywordSet =
			keywordSetHelper.findByCode (
				testSlice,
				"inbound");

		// auto responder

		AutoResponderRec testAutoResponder =
			autoResponderHelper.insert (
				new AutoResponderRec ()
					.setSlice (testSlice)
					.setCode ("test")
					.setName ("Test")
					.setDescription ("Test"));

		CommandRec testAutoResponderDefaultCommand =
			commandHelper.findByCode (
				testAutoResponder,
				"default");

		// keyword

		KeywordRec keyword =
			keywordHelper.insert (
				new KeywordRec ()
					.setKeywordSet (inboundKeywordSet)
					.setKeyword ("ar")
					.setDescription ("Auto responder")
					.setCommand (testAutoResponderDefaultCommand));

		database.flush ();

	}

	public
	void createManualResponderFixtures () {

		log.info (
			"Creating manual responder fixtures");

		Instant now =
			Instant.now ();

		// existing items

		RootRec root =
			rootHelper.find (0);

		SliceRec testSlice =
			sliceHelper.findByCode (
				GlobalId.root,
				"test");

		KeywordSetRec inboundKeywordSet =
			keywordSetHelper.findByCode (
				testSlice,
				"inbound");

		NetworkRec unknownNetwork =
			networkHelper.findByCode (
				GlobalId.root,
				"unknown");

		RouteRec freeRoute =
			routeHelper.findByCode (
				testSlice,
				"free");

		RouterRec freeRouter =
			routerHelper.findByCode (
				freeRoute,
				"static");

		// manual responder

		ManualResponderRec testManualResponder =
			manualResponderHelper.insert (
				new ManualResponderRec ()
					.setSlice (testSlice)
					.setCode ("test")
					.setName ("Test")
					.setDescription ("Test"));

		ManualResponderTemplateRec testManualResponderBlankTemplate =
			manualResponderTemplateHelper.insert (
				new ManualResponderTemplateRec ()
					.setManualResponder (testManualResponder)
					.setCode ("blank")
					.setName ("Blank")
					.setRouter (freeRouter)
					.setCustomisable (true)
					//.setDefaultMessages (1)
					.setMaximumMessages (1));

		CommandRec testManualResponderDefaultCommand =
			commandHelper.findByCode (
				testManualResponder,
				"default");

		// keyword

		KeywordRec keyword =
			keywordHelper.insert (
				new KeywordRec ()
					.setKeywordSet (inboundKeywordSet)
					.setKeyword ("mr")
					.setDescription ("Manual responder")
					.setCommand (testManualResponderDefaultCommand));

		database.flush ();

	}

	public
	void createAlertsFixtures () {

		MenuGroupRec facilityMenuGroup =
			menuGroupHelper.findByCode (
				GlobalId.root,
				"facility");

		MenuRec alertsMenu =
			objectManager.insert (
				new MenuRec ()
					.setMenuGroup (facilityMenuGroup)
					.setCode ("alerts")
					.setLabel ("Alerts")
					.setPath ("/alertsSettingss"));

	}

	Instant pastInstant (
			Instant now) {

		int choice =
			randomLogic.randomInteger (1000);

		return now.minus (
			choice * choice);

	}

	String[] alphabet =
		new String [] {

		"alpha",
		"beta",
		"charlie",
		"delta",
		"echo",
		"foxtrot",
		"golf",
		"hotel",
		"indigo",
		"juliet",
		"kilo",
		"mike",

		"november",
		"oscar",
		"papa",
		"quebec",
		"romeo",
		"sierra",
		"tango",
		"uniform",
		"violet",
		"whiskey",
		"xray",
		"yankee",
		"zulu"

	};

	*/

}
