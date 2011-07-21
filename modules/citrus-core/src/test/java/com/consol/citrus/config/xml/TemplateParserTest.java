/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.config.xml;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.container.Template;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class TemplateParserTest extends AbstractBeanDefinitionParserBaseTest<Template> {

    @Test
    public void testFailActionParser() {
        Map<String, Template> templates = beanDefinitionContext.getBeansOfType(Template.class);
        
        Assert.assertEquals(templates.size(), 2);
        
        Template template = templates.get("myTemplate");
        Assert.assertEquals(template.getName(), "template(myTemplate)");
        Assert.assertEquals(template.getParameter().size(), 0);
        Assert.assertEquals(template.getActions().size(), 1);
        Assert.assertEquals(template.isGlobalContext(), true);
        
        template = templates.get("my2ndTemplate");
        Assert.assertEquals(template.getName(), "template(my2ndTemplate)");
        Assert.assertEquals(template.getParameter().size(), 0);
        Assert.assertEquals(template.getActions().size(), 2);
        Assert.assertEquals(template.isGlobalContext(), false);
    }
}
