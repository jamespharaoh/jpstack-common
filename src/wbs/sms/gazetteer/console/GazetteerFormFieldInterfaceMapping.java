package wbs.sms.gazetteer.console;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCode;
import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gazetteer.model.GazetteerRec;

@Accessors (fluent = true)
@PrototypeComponent ("gazetteerFormFieldInterfaceMapping")
public
class GazetteerFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,GazetteerEntryRec,String> {

	// dependencies

	@Inject
	GazetteerEntryConsoleHelper gazetteerEntryHelper;

	@Inject
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String gazetteerFieldName;

	// implementation

	@Override
	public
	Either<Optional<GazetteerEntryRec>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				interfaceValue.get ())

		) {

			return successResult (
				Optional.<GazetteerEntryRec>absent ());

		}

		GazetteerRec gazetteer =
			(GazetteerRec)
			objectManager.dereference (
				container,
				gazetteerFieldName);

		if (
			isNull (
				gazetteer)
		) {

			return errorResult (
				stringFormat (
					"You must configure a gazetteer first"));

		}

		Optional<String> codeOptional =
			simplifyToCode (
				interfaceValue.get ());

		if (
			isNotPresent (
				codeOptional)
		) {

			return errorResult (
				stringFormat (
					"Location not found"));

		}

		GazetteerEntryRec entry =
			gazetteerEntryHelper.findByCode (
				gazetteer,
				codeOptional.get ());

		if (
			isNull (
				entry)
		) {

			return errorResult (
				stringFormat (
					"Location not found"));

		}

		return successResult (
			Optional.of (
				entry));

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Optional<GazetteerEntryRec> genericValue) {

		if (
			isPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					genericValue.get ().getName ()));

		} else {

			return successResult (
				Optional.<String>absent ());

		}

	}

}
