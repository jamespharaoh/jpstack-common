UPDATE shn_product_variant

SET cost_price_temp = events.value

FROM (

  SELECT
    event.id AS event_id,
    event_link_field_text.text AS field_name,
    event_link_target.ref_id AS target_id,
    event_link_value.ref_id AS value

  FROM event

  INNER JOIN event_link AS event_link_field
    ON event.id = event_link_field.event_id
    AND event_link_field.index = 1

  INNER JOIN text AS event_link_field_text
    ON event_link_field.ref_id = event_link_field_text.id

  INNER JOIN event_link AS event_link_target
    ON event.id = event_link_target.event_id
    AND event_link_target.index = 2
    AND event_link_target.type_id = 98

  INNER JOIN event_link AS event_link_value
    ON event.id = event_link_value.event_id
    AND event_link_value.index = 3

  WHERE

    event.event_type_id IN (20, 31)
    AND event_link_field_text.text = 'costPrice'

) AS events

WHERE shn_product_variant.id = events.target_id

;
