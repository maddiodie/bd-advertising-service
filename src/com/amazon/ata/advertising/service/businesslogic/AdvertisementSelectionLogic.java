package com.amazon.ata.advertising.service.businesslogic;

import com.amazon.ata.advertising.service.comparators.TargetingGroupComparator;
import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.model.AdvertisementContent;
import com.amazon.ata.advertising.service.model.EmptyGeneratedAdvertisement;
import com.amazon.ata.advertising.service.model.GeneratedAdvertisement;
import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.TargetingEvaluator;
import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * This class is responsible for picking the advertisement to be rendered.
 */
public class AdvertisementSelectionLogic {

    private static final Logger LOG = LogManager.getLogger(AdvertisementSelectionLogic.class);
    private final ReadableDao<String, List<AdvertisementContent>> contentDao;
    private final ReadableDao<String, List<TargetingGroup>> targetingGroupDao;
    private Random random = new Random();

    private TargetingEvaluator targetingEvaluator;
    private RequestContext requestContext;

    /**
     * Constructor for AdvertisementSelectionLogic.
     * @param contentDao Source of advertising content.
     * @param targetingGroupDao Source of targeting groups for each advertising content.
     */
    @Inject
    public AdvertisementSelectionLogic(ReadableDao<String, List<AdvertisementContent>> contentDao,
                                       ReadableDao<String, List<TargetingGroup>> targetingGroupDao) {
        this.contentDao = contentDao;
        this.targetingGroupDao = targetingGroupDao;
    }

    /**
     * Setter for Random class.
     * @param random generates random number used to select advertisements.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Gets all the content and metadata for the marketplace and determines which content can be shown.
     * Returns the eligible content with the highest click-through rate. If no advertisement is
     * available or eligible, returns an EmptyGeneratedAdvertisement.
     *
     * @param customerId - the customer to generate a custom advertisement for
     * @param marketplaceId - the id of the marketplace the advertisement will be rendered on
     * @return an advertisement customized for the customer id provided, or an empty advertisement if
     *       one could not be generated
     */
    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
//        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
//        if (StringUtils.isEmpty(marketplaceId)) {
//            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
//        } else {
//            final List<AdvertisementContent> contents = contentDao.get(marketplaceId);
//
//            if (CollectionUtils.isNotEmpty(contents)) {
//                AdvertisementContent randomAdvertisementContent = contents.get(random.nextInt(contents.size()));
//                generatedAdvertisement = new GeneratedAdvertisement(randomAdvertisementContent);
//            }
//
//        }

        // update so that this method only selects ads that the customer is eligible for based on
        //  the ad content's TargetingGroup
        // use streams to evaluate the TargetingGroups
        // use TargetingEvaluator to help filter out the ads that a customer is not eligible for
        // then randomly return one of the ads

        // if there are no eligible ads, return an EmptyGeneratedAdvertisement object

        // PLAN
        // (1) could use the content dao to get the list of advertisement content for the marketplace
        // (2) turn this into a stream
        // (3) evaluate each advertisement based on the targeting group
        //     - using the targeting evaluator, evaluate each targeting group? (based off of what, i
        //       don't know ...)
        // (4) then randomly return one of the ads (probably a stream method for this)
        // (5) throw the ifElse() method (i think) in there to return the emtpy generated advertisement

        GeneratedAdvertisement emptyGeneratedAdvertisement = new EmptyGeneratedAdvertisement();

        if (StringUtils.isEmpty(marketplaceId)) {
            LOG.warn("MarketplaceId cannot be null or empty. Returning an empty ad");
            return emptyGeneratedAdvertisement;
        }

        requestContext = new RequestContext(customerId, marketplaceId);
        targetingEvaluator = new TargetingEvaluator(requestContext);

        List<AdvertisementContent> contents = contentDao.get(marketplaceId);
        if (CollectionUtils.isEmpty(contents)) {
            return emptyGeneratedAdvertisement;
        }
        // fetch all advertisement contents for the marketplace

//        List<AdvertisementContent> eligibleAds = contents.stream()
//            .flatMap(advertisementContent ->
//                    targetingGroupDao.get(advertisementContent.getContentId()).stream()
//                            .sorted(new TargetingGroupComparator())
//                            .filter(targetingGroup ->
//                                    targetingEvaluator.evaluate(targetingGroup).isTrue())
//                            .map(targetingGroup -> advertisementContent))
//            .collect(Collectors.toList());
//        // stream to filter the eligible ads
        // VERSION 1

        List<AdvertisementContent> eligibleAds = contents.stream()
                .flatMap(advertisementContent ->
                        targetingGroupDao.get(advertisementContent.getContentId()).stream()
                                .sorted(new TargetingGroupComparator())
                                .filter(targetingGroup -> {
                                    boolean allPredicatesTrue =
                                            targetingGroup.getTargetingPredicates().stream()
                                                    .allMatch(predicate -> {
                                                        TargetingPredicateResult result =
                                                                predicate.evaluate(requestContext);
                                                        return result.isTrue();
                                                    });
                                    return allPredicatesTrue && targetingEvaluator
                                            .evaluate(targetingGroup).isTrue();
                                })
                                .map(targetingGroup -> advertisementContent))
                .collect(Collectors.toList());
        // stream to filter the eligible ads
        // VERSION 2

        if (eligibleAds.isEmpty()) {
            return emptyGeneratedAdvertisement;
        }

        return new GeneratedAdvertisement(eligibleAds.get(random.nextInt(eligibleAds.size())));
    }

}
