------------------------------------------------------------------------ hybyte

SELECT magic_number_set_insert ('hybyte_100', 'Hybyte block of 100');
SELECT magic_number_insert ('hybyte_100', '447781485301', '447781485389');
SELECT magic_number_insert ('hybyte_100', '447781485420', '447781485430');

SELECT magic_number_set_insert ('hybyte_voda_100', 'Hybyte Vodafone 100');
SELECT magic_number_insert ('hybyte_voda_100', '447786204105', '447786204204');

-------------------------------------------------------------------- mediaburst

-- mediaburst_gayx_primary

SELECT route_insert (
	'mediaburst_gayx_primary',
	'Mediaburst magic numbers (Gay Exchange, primary)',
	NULL,
	0,
	0,
	false,
	true,
	NULL,
	NULL);

UPDATE route
SET command_id = command ('root', 0, 'magic_number')
WHERE code = 'mediaburst_gayx_primary';

SELECT magic_number_set_insert (
	'mediaburst_gayx_primary',
	'Gay Exchange primary (Mediaburst, 100)');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800082',
	'447797800089');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800095',
	'447797800098');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800114',
	'447797800119');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800181',
	'447797800188');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800212',
	'447797800222');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800291',
	'447797800299');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800301',
	'447797800309');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800321',
	'447797800329');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800344',
	'447797800349');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800354',
	'447797800369');

SELECT magic_number_insert (
	'mediaburst_gayx_primary',
	'447797800456',
	'447797800469');

-- mediaburst_gayx_backup

SELECT route_insert (
	'mediaburst_gayx_backup',
	'Mediaburst magic numbers (Gay Exchange, backup)',
	NULL,
	0,
	0,
	false,
	true,
	NULL,
	NULL);

UPDATE route
SET command_id = command ('root', 0, 'magic_number')
WHERE code = 'mediaburst_gayx_backup';

SELECT magic_number_set_insert (
	'mediaburst_gayx_backup',
	'Gay Exchange backup (Mediaburst, 100)');

--SELECT magic_number_insert (
--	'mediaburst_gayx_backup',
--	'447624000053',
--	'447624000152');

SELECT magic_number_insert (
	'mediaburst_gayx_backup',
	'447624816875',
	'447624816974');
