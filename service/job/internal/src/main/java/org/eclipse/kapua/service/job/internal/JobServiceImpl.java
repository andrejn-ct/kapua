/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.job.internal;

import org.eclipse.kapua.KapuaDuplicateNameException;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableResourceLimitedService;
import org.eclipse.kapua.commons.model.query.predicate.AndPredicateImpl;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.job.engine.JobEngineService;
import org.eclipse.kapua.event.ListenServiceEvent;
import org.eclipse.kapua.event.RaiseServiceEvent;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.AttributePredicate.Operator;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.job.Job;
import org.eclipse.kapua.service.job.JobCreator;
import org.eclipse.kapua.service.job.JobFactory;
import org.eclipse.kapua.service.job.JobListResult;
import org.eclipse.kapua.service.job.JobPredicates;
import org.eclipse.kapua.service.job.JobQuery;
import org.eclipse.kapua.service.job.JobService;
import org.eclipse.kapua.service.scheduler.trigger.Trigger;
import org.eclipse.kapua.service.scheduler.trigger.TriggerFactory;
import org.eclipse.kapua.service.scheduler.trigger.TriggerListResult;
import org.eclipse.kapua.service.scheduler.trigger.TriggerPredicates;
import org.eclipse.kapua.service.scheduler.trigger.TriggerQuery;
import org.eclipse.kapua.service.scheduler.trigger.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JobService} implementation
 *
 * @since 1.0.0
 */
@KapuaProvider
public class JobServiceImpl extends AbstractKapuaConfigurableResourceLimitedService<Job, JobCreator, JobService, JobListResult, JobQuery, JobFactory> implements JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobServiceImpl.class);

    private final KapuaLocator locator = KapuaLocator.getInstance();

    private final JobEngineService jobEngineService = locator.getService(JobEngineService.class);
    private final TriggerService triggerService = locator.getService(TriggerService.class);
    private final TriggerFactory triggerFactory = locator.getFactory(TriggerFactory.class);
    private final AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
    private final PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);

    public JobServiceImpl() {
        super(JobService.class.getName(), JOB_DOMAIN, JobEntityManagerFactory.getInstance(), JobService.class, JobFactory.class);
    }

    @Override
    public Job create(JobCreator creator) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(creator, "jobCreator");
        ArgumentValidator.notNull(creator.getScopeId(), "jobCreator.scopeId");
        ArgumentValidator.notNull(creator.getName(), "jobCreator.name");

        //
        // Check access
        authorizationService.checkPermission(permissionFactory.newPermission(JOB_DOMAIN,Actions.write, creator.getScopeId()));

        //
        // Check limits
        if (allowedChildEntities(creator.getScopeId()) <= 0) {
            throw new KapuaIllegalArgumentException("scopeId", "max jobs reached");
        }

        //
        // Check duplicate name
        JobQuery query = new JobQueryImpl(creator.getScopeId());
        query.setPredicate(new AttributePredicateImpl<>(JobPredicates.NAME, creator.getName()));
        if (count(query) > 0) {
            throw new KapuaDuplicateNameException(creator.getName());
        }

        //
        // Do create
        return entityManagerSession.onTransactedInsert(em -> JobDAO.create(em, creator));
    }

    @Override
    public Job update(Job job) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(job, "job");
        ArgumentValidator.notNull(job.getScopeId(), "job.scopeId");
        ArgumentValidator.notNull(job.getName(), "job.name");

        //
        // Check access
        authorizationService.checkPermission(permissionFactory.newPermission(JOB_DOMAIN,Actions.write, job.getScopeId()));

        //
        // Check existence
        if (find(job.getScopeId(), job.getId()) == null) {
            throw new KapuaEntityNotFoundException(Job.TYPE, job.getId());
        }

        //
        // Check duplicate name
        JobQuery query = new JobQueryImpl(job.getScopeId());
        query.setPredicate(
                new AndPredicateImpl(
                        new AttributePredicateImpl<>(JobPredicates.NAME, job.getName()),
                        new AttributePredicateImpl<>(JobPredicates.ENTITY_ID, job.getId(), Operator.NOT_EQUAL)
                )
        );

        if (count(query) > 0) {
            throw new KapuaDuplicateNameException(job.getName());
        }

        //
        // Do update
        return entityManagerSession.onTransactedResult(em -> JobDAO.update(em, job));
    }

    @Override
    public Job find(KapuaId scopeId, KapuaId jobId) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(jobId, "jobId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(JOB_DOMAIN,Actions.write, scopeId));

        //
        // Do find
        return entityManagerSession.onResult(em -> JobDAO.find(em, jobId));
    }

    @Override
    public JobListResult query(KapuaQuery<Job> query) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(JOB_DOMAIN,Actions.read, query.getScopeId()));

        //
        // Do query
        return entityManagerSession.onResult(em -> JobDAO.query(em, query));
    }

    @Override
    public long count(KapuaQuery<Job> query) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(JOB_DOMAIN,Actions.read, query.getScopeId()));

        //
        // Do query
        return entityManagerSession.onResult(em -> JobDAO.count(em, query));
    }

    @Override
    @RaiseServiceEvent
    public void delete(KapuaId scopeId, KapuaId jobId) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(jobId, "jobId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(JOB_DOMAIN,Actions.delete, scopeId));

        //
        // Check existence
        if (find(scopeId, jobId) == null) {
            throw new KapuaEntityNotFoundException(Job.TYPE, jobId);
        }

        //
        // Find all the triggers that are associated with this job
        TriggerQuery query = triggerFactory.newQuery(scopeId);
        AndPredicateImpl andPredicate = new AndPredicateImpl()
                .and(new AttributePredicateImpl<>(TriggerPredicates.TRIGGER_PROPERTIES_NAME, "jobId"))
                .and(new AttributePredicateImpl<>(TriggerPredicates.TRIGGER_PROPERTIES_VALUE, jobId.toCompactId()))
                .and(new AttributePredicateImpl<>(TriggerPredicates.TRIGGER_PROPERTIES_TYPE, KapuaId.class.getName()));
        query.setPredicate(andPredicate);

        //
        // Query for and delete all the triggers that are associated with this job
        KapuaSecurityUtils.doPrivileged(() -> {
            TriggerListResult triggers = triggerService.query(query);
            for(Trigger trig : triggers.getItems()) {
                triggerService.delete(trig.getScopeId(), trig.getId());
            }
        });

        //
        // Do delete
        KapuaSecurityUtils.doPrivileged(() -> jobEngineService.cleanJobData(scopeId, jobId));
        entityManagerSession.onTransactedAction(em -> JobDAO.delete(em, jobId));
    }


    @ListenServiceEvent(fromAddress = "account")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            LOGGER.warn("JobService: Service bus error. Received null ServiceEvent");
        }
        LOGGER.info("JobService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());
        if ("org.eclipse.kapua.service.account.AccountService".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteJobsByAccountId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
    }

    // -----------------------------------------------------------------------------------------
    //
    // Private Methods
    //
    // -----------------------------------------------------------------------------------------

    private void deleteJobsByAccountId(KapuaId scopeId, KapuaId accountId) throws KapuaException {

        JobQuery query = new JobQueryImpl(accountId);
        JobListResult jobsToDelete = query(query);

        for (Job j : jobsToDelete.getItems()) {
            delete(j.getScopeId(), j.getId());
        }
    }

}
