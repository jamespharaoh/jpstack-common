package shn.shopify.apiclient.customcollection;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

import shn.shopify.apiclient.ShopifyApiRequestItem;
import shn.shopify.apiclient.metafield.ShopifyMetafieldRequest;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyCustomCollectionRequest
	implements ShopifyApiRequestItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataChild (
		name = "image")
	ShopifyCustomCollectionImageRequest image;

	@DataChildren (
		childrenElement = "metafields")
	List <ShopifyMetafieldRequest> metafields;

}
