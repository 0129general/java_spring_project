package com.salesmanager.core.business.services.user;

import java.util.List;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.common.Criteria;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.user.User;



public interface UserService extends SalesManagerEntityService<Long, User> {

  User getByUserName(String userName) throws ServiceException;
  User getByUserName(String userName, String storeCode) throws ServiceException;

  List<User> listUser() throws ServiceException;

  /**
   * Create or update a User
   * 
   * @param user
   * @throws ServiceException
   */
  void saveOrUpdate(User user) throws ServiceException;

  List<User> listByStore(MerchantStore store) throws ServiceException;

  User findByStore(Long userId, String storeCode) throws ServiceException;

  GenericEntityList<User> listByCriteria(Criteria criteria) throws ServiceException;




}
