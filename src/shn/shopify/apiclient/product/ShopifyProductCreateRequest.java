package shn.shopify.apiclient.product;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataClass;

import shn.shopify.apiclient.ShopifyApiClientCredentials;
import shn.shopify.apiclient.ShopifyApiRequest;
import shn.shopify.apiclient.ShopifyApiResponse;
import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductCreateRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	@DataChild (
		name = "product")
	ShopifyProductRequest product;

	// shopify api request implementation

	@Override
	public
	HttpMethod httpMethod () {
		return HttpMethod.post;
	}

	@Override
	public
	String httpPath () {
		return "/admin/products.json";
	}

	@Override
	public
	Class <? extends ShopifyApiResponse> httpResponseClass () {
		return ShopifyProductCreateResponse.class;
	}

}