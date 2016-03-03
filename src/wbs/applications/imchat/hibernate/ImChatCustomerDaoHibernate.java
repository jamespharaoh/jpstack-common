package wbs.applications.imchat.hibernate;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.applications.imchat.model.ImChatCustomerDao;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatCustomerSearch;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ImChatCustomerDaoHibernate
	extends HibernateDao
	implements ImChatCustomerDao {

	@Override
	public
	ImChatCustomerRec findByEmail (
			ImChatRec imChat,
			String email) {

		return findOne (
			ImChatCustomerRec.class,

			createCriteria (
				ImChatCustomerRec.class,
				"_imChatCustomer")

			.add (
				Restrictions.eq (
					"_imChatCustomer.imChat",
					imChat))

			.add (
				Restrictions.eq (
					"_imChatCustomer.email",
					email))

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			ImChatCustomerSearch imChatCustomerSearch) {

		Criteria criteria =

			createCriteria (
				ImChatCustomerRec.class,
				"_imChatCustomer")

			.createAlias (
				"_imChatCustomer.imChat",
				"_imChat");

		if (
			isNotNull (
				imChatCustomerSearch.imChatId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_imChat.id",
					imChatCustomerSearch.imChatId ()));

		}

		if (imChatCustomerSearch.code () != null) {

			criteria.add (
				Restrictions.eq (
					"_imChatCustomer.code",
					imChatCustomerSearch.code ()));

		}

		if (
			isNotNull (
				imChatCustomerSearch.email ())
		) {

			criteria.add (
				Restrictions.eq (
					"_imChatCustomer.email",
					imChatCustomerSearch.email ()));

		}

		if (
			isNotNull (
				imChatCustomerSearch.firstSession ())
		) {

			criteria.add (
				Restrictions.ge (
					"_imChatCustomer.firstSession",
					imChatCustomerSearch.firstSession ().getStart ()));

			criteria.add (
				Restrictions.lt (
					"_imChatCustomer.firstSession",
					imChatCustomerSearch.firstSession ().getEnd ()));

		}

		if (
			isNotNull (
				imChatCustomerSearch.lastSession ())
		) {

			criteria.add (
				Restrictions.ge (
					"_imChatCustomer.lastSession",
					imChatCustomerSearch.lastSession ().getStart ()));

			criteria.add (
				Restrictions.lt (
					"_imChatCustomer.lastSession",
					imChatCustomerSearch.lastSession ().getEnd ()));

		}

		// set order

		switch (imChatCustomerSearch.order ()) {

		case timestampDesc:

			criteria

				.addOrder (
					Order.desc ("_imChatCustomer.lastSession"));

			break;

		case totalPurchaseDesc:

			criteria

				.addOrder (
					Order.desc ("_imChatCustomer.totalPurchase"));

			break;

		case balanceDesc:

			criteria

				.addOrder (
					Order.desc ("_imChatCustomer.balance"));

			break;

		default:

			throw new RuntimeException (
				"should never happen");

		}

		// set to return ids only

		criteria

			.setProjection (
				Projections.id ());

		// perform and return

		return findMany (
			Integer.class,
			criteria.list ());

	}

}
