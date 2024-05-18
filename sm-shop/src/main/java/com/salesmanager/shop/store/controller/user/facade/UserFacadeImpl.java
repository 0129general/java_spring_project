package com.salesmanager.shop.store.controller.user.facade;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.user.PermissionService;
import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.model.common.Criteria;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.Permission;
import com.salesmanager.core.model.user.User;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.model.security.ReadableGroup;
import com.salesmanager.shop.model.security.ReadablePermission;
import com.salesmanager.shop.model.user.PersistableUser;
import com.salesmanager.shop.model.user.ReadableUser;
import com.salesmanager.shop.model.user.ReadableUserList;
import com.salesmanager.shop.model.user.UserPassword;
import com.salesmanager.shop.populator.user.PersistableUserPopulator;
import com.salesmanager.shop.populator.user.ReadableUserPopulator;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.category.ShoppingCategoryController;
import com.salesmanager.shop.store.controller.security.facade.SecurityFacade;

@Service("userFacade")
public class UserFacadeImpl implements UserFacade {
  
  @Inject
  private MerchantStoreService merchantStoreService;

  @Inject
  private UserService userService;

  @Inject
  private PermissionService permissionService;

  @Inject
  private LanguageService languageService;

  @Inject
  private PersistableUserPopulator persistableUserPopulator;
  
  @Inject 
  private SecurityFacade securityFacade;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(UserFacadeImpl.class);
  

  @Override
  public ReadableUser findByUserName(String userName, String storeCode, Language lang) {
    User user = getByUserName(userName, storeCode);
    if (user == null) {
      throw new ResourceNotFoundException("User [" + userName + "] not found");
    }
    return convertUserToReadableUser(lang, user);
  }
  
  private ReadableUser findByUserName(String userName, Language lang) {
    User user = getByUserName(userName);
    if (user == null) {
      throw new ResourceNotFoundException("User [" + userName + "] not found");
    }
    return convertUserToReadableUser(lang, user);
  }

  private ReadableUser convertUserToReadableUser(Language lang, User user) {
    ReadableUserPopulator populator = new ReadableUserPopulator();
    try {
      ReadableUser readableUser = new ReadableUser();
      readableUser = populator.populate(user, readableUser, user.getMerchantStore(), lang);


      List<Integer> groupIds = readableUser.getGroups().stream().map(ReadableGroup::getId).map(Long::intValue).collect(Collectors.toList());
      List<ReadablePermission> permissions = findPermissionsByGroups(groupIds);
      readableUser.setPermissions(permissions);
      
      return readableUser;
    } catch (ConversionException e) {
      throw new ConversionRuntimeException(e);
    }
  }

  private User converPersistabletUserToUser(MerchantStore store, Language lang,
      User userModel, PersistableUser user) {
    try {
      return persistableUserPopulator.populate(user, userModel, store, lang);
    } catch (ConversionException e) {
      throw new ConversionRuntimeException(e);
    }
  }

  private User getByUserName(String userName) {
    try {
      return userService.getByUserName(userName);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }
  
  private User getByUserName(String userName, String storeCode) {
    try {
      return userService.getByUserName(userName, storeCode);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }
  
  private User getByUserId(Long id, String storeCode) {
    try {
      return userService.findByStore(id, storeCode);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }


  @Override
  public List<ReadablePermission> findPermissionsByGroups(List<Integer> ids) {
    return getPermissionsByIds(ids).stream()
        .map(permission -> convertPermissionToReadablePermission(permission))
        .collect(Collectors.toList());
  }

  private ReadablePermission convertPermissionToReadablePermission(Permission permission) {
    ReadablePermission readablePermission = new ReadablePermission();
    readablePermission.setId(Long.valueOf(permission.getId()));
    readablePermission.setName(permission.getPermissionName());
    return readablePermission;
  }

  private List<Permission> getPermissionsByIds(List<Integer> ids) {
    try {
      return permissionService.getPermissions(ids);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }

  @Override
  public boolean authorizedStore(String userName, String merchantStoreCode) {

    try {
      ReadableUser readableUser = findByUserName(userName, languageService.defaultLanguage());

      // unless superadmin
      for (ReadableGroup group : readableUser.getGroups()) {
        if (Constants.GROUP_SUPERADMIN.equals(group.getName())) {
          return true;
        }
      }


      boolean authorized = false;
      User user = userService.findByStore(readableUser.getId(), merchantStoreCode);
      if (user != null) {
        authorized = true;
      }

      return authorized;
    } catch (Exception e) {
      throw new ServiceRuntimeException(
          "Cannot authorize user " + userName + " for store " + merchantStoreCode, e.getMessage());
    }
  }



  @Override
  public void authorizedGroup(String userName, List<String> groupName) {

    ReadableUser readableUser = findByUserName(userName, languageService.defaultLanguage());

    // unless superadmin
    for (ReadableGroup group : readableUser.getGroups()) {
      if (groupName.contains(group.getName())) {
        return;
      }
    }

    throw new ServiceRuntimeException("User " + userName + " not authorized");

  }

  @Override
  public String authenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUserName = authentication.getName();
      return currentUserName;
    }
    return null;
  }

  @Override
  public ReadableUser create(PersistableUser user, MerchantStore store) {
    User userModel = new User();
    userModel = converPersistabletUserToUser(store, languageService.defaultLanguage(), userModel, user);
    if (CollectionUtils.isEmpty(userModel.getGroups())) {
      throw new ServiceRuntimeException(
          "No valid group groups associated with user " + user.getUserName());
    }
    try {
      userService.saveOrUpdate(userModel);
      //now build returned object
      User createdUser = userService.getById(userModel.getId());
      return convertUserToReadableUser(createdUser.getDefaultLanguage(), createdUser);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(
          "Cannot create user " + user.getUserName() + " for store " + store.getCode(), e);
    }
  }

  @Override
  public ReadableUserList getByCriteria(Language language, String drawParam, Criteria criteria) {
    try {
      ReadableUserList readableUserList = new ReadableUserList();
      GenericEntityList<User> userList = userService.listByCriteria(criteria);
      for (User user : userList.getList()) {
        ReadableUser readableUser = this.convertUserToReadableUser(language, user);
        readableUserList.getData().add(readableUser);
      }
      readableUserList.setRecordsTotal(userList.getTotalCount());
      readableUserList.setTotalCount(readableUserList.getData().size());
      readableUserList.setRecordsFiltered(userList.getTotalCount());
      if (!org.apache.commons.lang3.StringUtils.isEmpty(drawParam)) {
        readableUserList.setDraw(Integer.parseInt(drawParam));
      }
      return readableUserList;
    } catch (ServiceException e) {
      throw new ServiceRuntimeException("Cannot get users by criteria user", e);
    }
  }

  @Override
  public void delete(Long id, String merchant) {
    Validate.notNull(id, "User id cannot be null");

    try {
      User user = userService
          .findByStore(id, merchant);
      if (user == null) {
        throw new ServiceRuntimeException("Cannot find user [" + id + "]");
      }
      
      //cannot delete superadmin
      if(user.getGroups().contains(Constants.GROUP_SUPERADMIN)) {
        throw new ServiceRuntimeException("Cannot delete superadmin user [" + id + "]");
      }
      
      userService.delete(user);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException("Cannot find user [" + id + "]", e);
    }

  }

  @Override
  public ReadableUser update(Long id, String authenticatedUser, String storeCode, PersistableUser user) {
    Validate.notNull(user, "User cannot be null");

    try {
      User userModel = userService.findByStore(id, storeCode);
      if (userModel == null) {
        throw new ServiceRuntimeException("Cannot find user [" + user.getUserName() + "]");
      }
      if(userModel.getId().longValue() != id.longValue()) {
        throw new ServiceRuntimeException("Cannot find user [" + user.getUserName() + "] id or name does not match");
      }
      User auth = userService.getByUserName(authenticatedUser);
      if (auth == null) {
        throw new ServiceRuntimeException("Cannot find user [" + authenticatedUser + "]");
      }
      boolean isActive = userModel.isActive();
      List<Group> originalGroups = userModel.getGroups();
      Group superadmin = originalGroups.stream()
          .filter(group -> Constants.GROUP_SUPERADMIN.equals(group.getGroupName()))
          .findAny()
          .orElse(null);
      MerchantStore store = merchantStoreService.getByCode(storeCode);
      userModel = converPersistabletUserToUser(store, languageService.defaultLanguage(), userModel, user);
      
      //if superadmin set original permissions, prevent removing super admin
      if(superadmin!=null) {
        userModel.setGroups(originalGroups);
      }
      
      Group adminGroup = auth.getGroups().stream()
          .filter((group) -> Constants.GROUP_SUPERADMIN.equals(group.getGroupName()) || Constants.GROUP_SUPERADMIN.equals(group.getGroupName()))
          .findAny()
          .orElse(null);
      
      if(adminGroup == null) {
        userModel.setGroups(originalGroups);
        userModel.setActive(isActive);
      }
      

      user.setPassword(userModel.getAdminPassword());
      userService.saveOrUpdate(userModel);
      return this.convertUserToReadableUser(languageService.defaultLanguage(), userModel);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException("Cannot update user [" + user.getUserName() + "]", e);
    }
    
  }

  @Override
  public ReadableUser findById(Long id, String storeCode, Language lang) {
    User user = getByUserId(id, storeCode);
    if (user == null) {
      throw new ResourceNotFoundException("User [" + id + "] not found");
    }
    return convertUserToReadableUser(lang, user);
  }

  @Override
  public void changePassword(Long userId, String authenticatedUser, String storeCode,
      UserPassword changePassword) {
    
    Validate.notNull(changePassword, "Change password request must not be null");
    Validate.notNull(changePassword.getPassword(), "Original password request must not be null");
    Validate.notNull(changePassword.getChangePassword(), "New password request must not be null");
    
    /**
     * Only admin and superadmin can change other user password
     */
    User auth = null;
    try {
      auth = userService.getByUserName(authenticatedUser);

      if (auth == null) {
        throw new ServiceRuntimeException("Cannot find user [" + authenticatedUser + "]");
      }
      
      User userModel = userService.getById(userId);
      if (userModel == null) {
        throw new ServiceRuntimeException("Cannot find user [" + userId + "]");
      }

      /**
       * need to validate if actual password match
       */
      
      if(!securityFacade.matchPassword(userModel.getAdminPassword(), changePassword.getPassword())) {
        throw new ServiceRuntimeException("Actual password does not match for user [" + userId + "]");
      }
          
      /**
       * Validate new password
       */
      if(!securityFacade.validateUserPassword(changePassword.getChangePassword())) {
        throw new ServiceRuntimeException("New password does not apply to format policy");
      }
      
      String newPasswordEncoded = securityFacade.encodePassword(changePassword.getChangePassword());
      userModel.setAdminPassword(newPasswordEncoded);
      
      userService.update(userModel);
    
    
    } catch (ServiceException e) {
      LOGGER.error("Error updating password");
      throw new ServiceRuntimeException(e);
    }
    
    
    
  }

}
