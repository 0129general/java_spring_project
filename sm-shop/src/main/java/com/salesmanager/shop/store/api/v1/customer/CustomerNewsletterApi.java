package com.salesmanager.shop.store.api.v1.customer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.optin.PersistableCustomerOptin;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.utils.LanguageUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;


/**
 * Optin a customer to newsletter
 * @author carlsamson
 *
 */
@RestController
@RequestMapping(value = "/api/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerNewsletterApi {

	@Inject
	private CustomerFacade customerFacade;

	@Inject
	private StoreFacade storeFacade;
	
	@Inject
	private LanguageUtils languageUtils;

  /** Create new optin */
  @PostMapping("/newsletter")
  @ApiOperation(
      httpMethod = "POST",
      value = "Creates a newsletter optin",
      notes = "",
      produces = "application/json")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
      @ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en")
  })
  public void create(
      @Valid @RequestBody PersistableCustomerOptin optin,
      @ApiIgnore MerchantStore merchantStore,
      @ApiIgnore Language language) {
		customerFacade.optinCustomer(optin, merchantStore);
	}

  @PutMapping("/newsletter/{email}")
  @ApiOperation(
      httpMethod = "PUT",
      value = "Updates a customer",
      notes = "Requires administration access",
      produces = "application/json",
      response = PersistableCustomer.class)
  public void update(
      @PathVariable String email,
      @Valid @RequestBody PersistableCustomer customer,
      HttpServletRequest request,
      HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }

  @DeleteMapping("/newsletter/{email}")
  @ApiOperation(
      httpMethod = "DELETE",
      value = "Deletes a customer",
      notes = "Requires administration access",
      response = Void.class)
  public ResponseEntity<Void> delete(
      @PathVariable String email, HttpServletRequest request, HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }
}
