package shn.shopify.fixture;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.logging.LogContext;

import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

import shn.shopify.model.ShnShopifyConnectionObjectHelper;

public
class ShnShopifyFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnShopifyConnectionObjectHelper shopifyConnectionHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TestAccounts testAccounts;

	@SingletonDependency
	WbsConfig wbsConfig;

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			createMenuItems (
				transaction);

			createConnections (
				transaction);

		}

	}

	// private implementation

	private
	void createMenuItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"shopping_nation"))

				.setCode (
					"shopify")

				.setName (
					"Shopify")

				.setDescription (
					"Shopify")

				.setLabel (
					"Shopify")

				.setTargetPath (
					"/shnShopify")

				.setTargetFrame (
					"main")

			);

			transaction.flush ();

		}

	}

	private
	void createConnections (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createConnections");

		) {

			Set <String> ignoreParams =
				ImmutableSet.of (
					"slice");

			testAccounts.forEach (
				"shn-shopify-connection",
				suppliedParams -> {

				SliceRec slice =
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						mapItemForKeyRequired (
							suppliedParams,
							"slice"));

				Map <String, String> allParams =
					ImmutableMap.<String, String> builder ()

					.putAll (
						suppliedParams)

					.put (
						"code",
						simplifyToCodeRequired (
							mapItemForKeyRequired (
								suppliedParams,
								"name")))

					.build ();

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"SHN Shopify",
					shopifyConnectionHelper,
					slice,
					allParams,
					ignoreParams);

			});

			transaction.flush ();

		}

	}

}
