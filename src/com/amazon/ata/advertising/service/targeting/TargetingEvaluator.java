package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.List;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {

    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = false;
    private final RequestContext requestContext;

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the
     * given RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE
     * otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {
//        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
//        boolean allTruePredicates = true;
//        for (TargetingPredicate predicate : targetingPredicates) {
//            TargetingPredicateResult predicateResult = predicate.evaluate(requestContext);
//            if (!predicateResult.isTrue()) {
//                allTruePredicates = false;
//                break;
//            }
              // allTruePredicates changes to false if one of them is false and breaks at that point
//        }

        boolean allTruePredicates = targetingGroup.getTargetingPredicates()
                // retrieves the list of TargetingGroup objects from the targetGroup
                .stream()
                // converts the list of objects into a stream
                .map(predicate -> predicate.evaluate(requestContext))
                // evaluates each TargetingPredicate in the stream against the requestContext
                // evaluate() method is called on each predicate and returns a TargetingPredicateResult
                // map() is taking the predicate from the stream, evaluating it, and producing a new
                //  stream of TargetingPredicateResult objects
                .allMatch(TargetingPredicateResult::isTrue);
                // checks if all the elements of the stream of TargetingPredicateResult objects is
                //  satisfied by the condition from the isTrue() method
                // TargetingPredicateResult::isTrue is a method reference that is the same as what you
                //  had previously ... <predicate -> predicateResult.isTrue()>
                // it checks if the isTrue() method returns true for each TargetingPredicateResult in
                //  the stream
                // if all the TargetingPredicateResult objects in the stream return true for isTrue()
                //  then allMatch() returns true, otherwise it returns false

        return allTruePredicates ? TargetingPredicateResult.TRUE :
                                   TargetingPredicateResult.FALSE;
        // ternary operator
    }

}
