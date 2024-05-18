package com.salesmanager.shop.store.controller.store.facade;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.Validate;
import org.drools.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.content.ContentService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.reference.zone.ZoneService;
import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.constants.MeasureUnit;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;
import com.salesmanager.shop.model.content.ReadableImage;
import com.salesmanager.shop.model.shop.MerchantConfigEntity;
import com.salesmanager.shop.model.shop.PersistableBrand;
import com.salesmanager.shop.model.shop.PersistableMerchantStore;
import com.salesmanager.shop.model.shop.ReadableBrand;
import com.salesmanager.shop.model.shop.ReadableMerchantStore;
import com.salesmanager.shop.model.shop.ReadableMerchantStoreList;
import com.salesmanager.shop.populator.store.PersistableMerchantStorePopulator;
import com.salesmanager.shop.populator.store.ReadableMerchantStorePopulator;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.utils.ImageFilePath;
import com.salesmanager.shop.utils.LanguageUtils;

@Service("storeFacade")
public class StoreFacadeImpl implements StoreFacade {

  @Inject
  private MerchantStoreService merchantStoreService;

  @Inject
  private MerchantConfigurationService merchantConfigurationService;

  @Inject
  private LanguageService languageService;

  @Inject
  private CountryService countryService;

  @Inject
  private ZoneService zoneService;

  @Inject
  private ContentService contentService;

  @Inject
  private PersistableMerchantStorePopulator persistableMerchantStorePopulator;

  @Inject
  @Qualifier("img")
  private ImageFilePath imageUtils;

  @Inject
  private LanguageUtils languageUtils;

  private static final Logger LOG = LoggerFactory.getLogger(StoreFacadeImpl.class);

  @Override
  public MerchantStore getByCode(HttpServletRequest request) {
    String code = request.getParameter("store");
    if (StringUtils.isEmpty(code)) {
      code = com.salesmanager.core.business.constants.Constants.DEFAULT_STORE;
    }
    return get(code);
  }

  @Override
  public MerchantStore get(String code) {
    try {
      return merchantStoreService.getByCode(code);
    } catch (ServiceException e) {
      LOG.error("Error while getting MerchantStore", e);
      throw new ServiceRuntimeException(e);
    }

  }

  @Override
  public ReadableMerchantStore getByCode(String code, String lang) {
    Language language = getLanguage(lang);
    return getByCode(code, language);
  }

  private Language getLanguage(String lang) {
    return languageUtils.getServiceLanguage(lang);
  }

  @Override
  public ReadableMerchantStore getByCode(String code, Language language) {
    MerchantStore store = getMerchantStoreByCode(code);
    return convertMerchantStoreToReadableMerchantStore(language, store);
  }

  @Override
  public boolean existByCode(String code) {
    try {
      return merchantStoreService.getByCode(code) != null;
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }

  private ReadableMerchantStore convertMerchantStoreToReadableMerchantStore(Language language,
      MerchantStore store) {
    ReadableMerchantStore readable = new ReadableMerchantStore();

    ReadableMerchantStorePopulator populator = new ReadableMerchantStorePopulator();
    populator.setCountryService(countryService);
    populator.setZoneService(zoneService);
    populator.setFilePath(imageUtils);

    /**
     * Language is not important for this conversion using default language
     */
    try {
      readable = populator.populate(store, readable, store, language);
    } catch (Exception e) {
      throw new ConversionRuntimeException(
          "Error while populating MerchantStore " + e.getMessage());
    }
    return readable;
  }

  private MerchantStore getMerchantStoreByCode(String code) {
    return Optional.ofNullable(get(code))
        .orElseThrow(() -> new ResourceNotFoundException("Merchant store code not found"));
  }

  @Override
  public ReadableMerchantStore create(PersistableMerchantStore store) {

    Validate.notNull(store, "PersistableMerchantStore must not be null");
    Validate.notNull(store.getCode(), "PersistableMerchantStore.code must not be null");

    // check if store code exists
    MerchantStore storeForCheck = get(store.getCode());
    if (storeForCheck != null) {
      throw new ServiceRuntimeException("MerhantStore " + store.getCode() + " already exists");
    }

    MerchantStore mStore =
        convertPersistableMerchantStoreToMerchantStore(store, languageService.defaultLanguage());
    createMerchantStore(mStore);

    ReadableMerchantStore storeTO = getByCode(store.getCode(), languageService.defaultLanguage());
    return storeTO;
  }

  private void createMerchantStore(MerchantStore mStore) {
    try {
      merchantStoreService.create(mStore);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }

  private MerchantStore convertPersistableMerchantStoreToMerchantStore(
      PersistableMerchantStore store, Language language) {
    MerchantStore mStore = new MerchantStore();

    // set default values
    mStore.setWeightunitcode(MeasureUnit.KG.name());
    mStore.setSeizeunitcode(MeasureUnit.IN.name());

    try {
      mStore = persistableMerchantStorePopulator.populate(store, mStore, language);
    } catch (ConversionException e) {
      throw new ConversionRuntimeException(e);
    }
    return mStore;
  }

  @Override
  public ReadableMerchantStore update(PersistableMerchantStore store) {

    Validate.notNull(store);

    MerchantStore mStore = mergePersistableMerchantStoreToMerchantStore(store, store.getCode(),
        languageService.defaultLanguage());

    updateMerchantStore(mStore);

    ReadableMerchantStore storeTO = getByCode(store.getCode(), languageService.defaultLanguage());
    return storeTO;
  }

  private void updateMerchantStore(MerchantStore mStore) {
    try {
      merchantStoreService.update(mStore);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }

  }

  private MerchantStore mergePersistableMerchantStoreToMerchantStore(PersistableMerchantStore store,
      String code, Language language) {

    MerchantStore mStore = getMerchantStoreByCode(code);

    store.setId(mStore.getId());

    try {
      mStore = persistableMerchantStorePopulator.populate(store, mStore, language);
    } catch (ConversionException e) {
      throw new ConversionRuntimeException(e);
    }
    return mStore;
  }

  @Override
  public ReadableMerchantStoreList getByCriteria(MerchantStoreCriteria criteria, String drawParam,
      Language lang) {
    GenericEntityList<MerchantStore> entityList = getMerchantStoresByCriteria(criteria);

    List<MerchantStore> stores = entityList.getList();

    return createReadableMerchantStoreList(drawParam, lang, entityList, stores);
  }

  private ReadableMerchantStoreList createReadableMerchantStoreList(String drawParam, Language lang,
      GenericEntityList<MerchantStore> list, List<MerchantStore> stores) {
    ReadableMerchantStoreList merchantStoreToList = new ReadableMerchantStoreList();
    merchantStoreToList.setTotalCount(list.getTotalCount());

    List<ReadableMerchantStore> readableMerchantStores =
        stores.stream().map(store -> convertMerchantStoreToReadableMerchantStore(lang, store))
            .collect(Collectors.toList());

    merchantStoreToList.getData().addAll(readableMerchantStores);

    merchantStoreToList.setRecordsFiltered(merchantStoreToList.getTotalCount());
    merchantStoreToList.setRecordsTotal(merchantStoreToList.getTotalCount());

    if (!org.apache.commons.lang3.StringUtils.isEmpty(drawParam)) {
      merchantStoreToList.setDraw(Integer.parseInt(drawParam));
    }
    return merchantStoreToList;
  }

  private GenericEntityList<MerchantStore> getMerchantStoresByCriteria(
      MerchantStoreCriteria criteria) {
    try {
      return Optional.ofNullable(merchantStoreService.getByCriteria(criteria))
          .orElseThrow(() -> new ResourceNotFoundException("Criteria did not match any store"));
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }

  }

  @Override
  public void delete(String code) {

    if (MerchantStore.DEFAULT_STORE.equals(code.toUpperCase())) {
      throw new ServiceRuntimeException("Cannot remove default store");
    }

    MerchantStore mStore = getMerchantStoreByCode(code);

    try {
      merchantStoreService.delete(mStore);
    } catch (Exception e) {
      LOG.error("Error while deleting MerchantStore", e);
      throw new ServiceRuntimeException("Error while deleting MerchantStore " + e.getMessage());
    }

  }

  @Override
  public ReadableBrand getBrand(String code) {
    MerchantStore mStore = getMerchantStoreByCode(code);

    ReadableBrand readableBrand = new ReadableBrand();
    if (!StringUtils.isEmpty(mStore.getStoreLogo())) {
      String imagePath = imageUtils.buildStoreLogoFilePath(mStore);
      ReadableImage image = createReadableImage(mStore.getStoreLogo(), imagePath);
      readableBrand.setLogo(image);
    }
    List<MerchantConfigEntity> merchantConfigTOs = getMerchantConfigEntities(mStore);
    readableBrand.getSocialNetworks().addAll(merchantConfigTOs);
    return readableBrand;
  }

  private List<MerchantConfigEntity> getMerchantConfigEntities(MerchantStore mStore) {
    List<MerchantConfiguration> configurations =
        getMergeConfigurationsByStore(MerchantConfigurationType.SOCIAL, mStore);

    return configurations.stream().map(config -> convertToMerchantConfigEntity(config))
        .collect(Collectors.toList());
  }

  private List<MerchantConfiguration> getMergeConfigurationsByStore(
      MerchantConfigurationType configurationType, MerchantStore mStore) {
    try {
      return merchantConfigurationService.listByType(configurationType, mStore);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(
          "Error wile getting merchantConfigurations " + e.getMessage());
    }
  }

  private MerchantConfigEntity convertToMerchantConfigEntity(MerchantConfiguration config) {
    MerchantConfigEntity configTO = new MerchantConfigEntity();
    configTO.setId(config.getId());
    configTO.setKey(config.getKey());
    configTO.setType(config.getMerchantConfigurationType());
    configTO.setValue(config.getValue());
    configTO.setActive(config.getActive() != null ? config.getActive().booleanValue() : false);
    return configTO;
  }

  private MerchantConfiguration convertToMerchantConfiguration(MerchantConfigEntity config,
      MerchantConfigurationType configurationType) {
    MerchantConfiguration configTO = new MerchantConfiguration();
    configTO.setId(config.getId());
    configTO.setKey(config.getKey());
    configTO.setMerchantConfigurationType(configurationType);
    configTO.setValue(config.getValue());
    configTO.setActive(new Boolean(config.isActive()));
    return configTO;
  }

  private ReadableImage createReadableImage(String storeLogo, String imagePath) {
    ReadableImage image = new ReadableImage();
    image.setName(storeLogo);
    image.setPath(imagePath);
    return image;
  }

  @Override
  public void deleteLogo(String code) {
    MerchantStore store = getByCode(code);
    String image = store.getStoreLogo();
    store.setStoreLogo(null);

    try {
      updateMerchantStore(store);
      if (!StringUtils.isEmpty(image)) {
        contentService.removeFile(store.getCode(), image);
      }
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e.getMessage());
    }
  }

  @Override
  public MerchantStore getByCode(String code) {
    return getMerchantStoreByCode(code);
  }

  @Override
  public void addStoreLogo(String code, InputContentFile cmsContentImage) {
    MerchantStore store = getByCode(code);
    store.setStoreLogo(cmsContentImage.getFileName());
    saveMerchantStore(store);
    addLogoToStore(code, cmsContentImage);
  }

  private void addLogoToStore(String code, InputContentFile cmsContentImage) {
    try {
      contentService.addLogo(code, cmsContentImage);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }
  }

  private void saveMerchantStore(MerchantStore store) {
    try {
      merchantStoreService.save(store);
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e);
    }

  }

  @Override
  public void createBrand(String merchantStoreCode, PersistableBrand brand) {
    MerchantStore mStore = getMerchantStoreByCode(merchantStoreCode);

    List<MerchantConfigEntity> createdConfigs = brand.getSocialNetworks();

    List<MerchantConfiguration> configurations = createdConfigs.stream()
        .map(config -> convertToMerchantConfiguration(config, MerchantConfigurationType.SOCIAL))
        .collect(Collectors.toList());
    try {
      for (MerchantConfiguration mConfigs : configurations) {
        mConfigs.setMerchantStore(mStore);
        if (!StringUtils.isEmpty(mConfigs.getValue())) {
          mConfigs.setMerchantConfigurationType(MerchantConfigurationType.SOCIAL);
          merchantConfigurationService.saveOrUpdate(mConfigs);
        } else {// remove if submited blank and exists
          MerchantConfiguration config =
              merchantConfigurationService.getMerchantConfiguration(mConfigs.getKey(), mStore);
          if (config != null) {
            merchantConfigurationService.delete(config);
          }
        }
      }
    } catch (ServiceException se) {
      throw new ServiceRuntimeException(se);
    }

  }

}
