/*
 * Copyright 2025, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.cdi.tck.tests.implementation.simple.resource.persistenceAgent;

import static org.jboss.cdi.tck.TestGroups.INTEGRATION;
import static org.jboss.cdi.tck.TestGroups.PERSISTENCE;
import static org.jboss.cdi.tck.cdi.Sections.DECLARING_RESOURCE;
import static org.jboss.cdi.tck.cdi.Sections.RESOURCE_LIFECYCLE;
import static org.jboss.cdi.tck.cdi.Sections.RESOURCE_TYPES;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityHandler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.cdi.tck.AbstractTest;
import org.jboss.cdi.tck.util.Assert;
import org.jboss.cdi.tck.shrinkwrap.ee.WebArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecAssertions;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

@Test(groups = { INTEGRATION, PERSISTENCE })
@SpecVersion(spec = "cdi", version = "5.0")
public class PersistenceAgentInjectionTest extends AbstractTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return new WebArchiveBuilder().withTestClassPackage(PersistenceAgentInjectionTest.class).withBeansXml("beans.xml")
                .withDefaultPersistenceXml().build();
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = DECLARING_RESOURCE, id = "cc"), @SpecAssertion(section = RESOURCE_LIFECYCLE, id = "lb") })
    public void testInjectionOfPersistenceAgent() {
        Bean<ManagedBean> managedBeanBean = getBeans(ManagedBean.class).iterator().next();
        CreationalContext<ManagedBean> managedBeanCc = getCurrentManager().createCreationalContext(managedBeanBean);
        ManagedBean managedBean = managedBeanBean.create(managedBeanCc);
        assert managedBean.getPersistenceAgent() != null : "Persistence agent was not injected into bean";
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = RESOURCE_LIFECYCLE, id = "md") })
    public void testPassivationOfPersistenceAgent() throws Exception {
        Bean<ManagedBean> managedBeanBean = getBeans(ManagedBean.class).iterator().next();
        CreationalContext<ManagedBean> managedBeanCc = getCurrentManager().createCreationalContext(managedBeanBean);
        ManagedBean managedBean = managedBeanBean.create(managedBeanCc);
        managedBean = (ManagedBean) activate(passivate(managedBean));
        assert managedBean.getPersistenceAgent() != null : "Persistence agent was not injected into bean";
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = RESOURCE_TYPES, id = "ab") })
    public void testBeanTypesOfPersistenceAgent() {
        Bean<EntityAgent> agent = getBeans(EntityAgent.class, new Database.Literal()).iterator().next();
        assert agent.getTypes().size() == 4;
        Assert.assertTypesMatch(agent.getTypes(), EntityAgent.class, EntityHandler.class, AutoCloseable.class, Object.class);
    }
}
