/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import static org.easymock.EasyMock.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class SendMessageDefinitionTest extends AbstractTestNGUnitTest {
    
    private MessageSender messageSender = EasyMock.createMock(MessageSender.class);
    
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    
    @Test
    public void testSendBuilder() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build());
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "Foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");
    }
    
    @Test
    public void testSendBuilderWithPayloadData() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
    }
    
    @Test
    public void testSendBuilderWithPayloadResource() throws IOException {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload(resource);
            }
        };
        
        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("somePayloadData".getBytes())).once();
        replay(resource);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "somePayloadData");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        verify(resource);
    }
    
    @Test
    public void testSendBuilderWithSenderName() {
        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                send("fooMessageSender")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        reset(applicationContextMock);
        
        expect(applicationContextMock.getBean("fooMessageSender", MessageSender.class)).andReturn(messageSender).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        
        replay(applicationContextMock);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageSender(), messageSender);
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testSendBuilderWithHeaders() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("operation", "foo")
                    .header("language", "eng");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 2L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("language"), "eng");
    }
    
    @Test
    public void testSendBuilderWithHeaderData() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
                
                send(messageSender)
                    .message(MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build())
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getMessageHeaderData(), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertNull(messageBuilder.getMessageHeaderResourcePath());
        
        action = ((SendMessageAction)builder.testCase().getActions().get(1));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getMessageHeaderData(), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertNull(messageBuilder.getMessageHeaderResourcePath());
    }
    
    @Test
    public void testSendBuilderWithHeaderDataResource() throws IOException {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header(resource);
                
                send(messageSender)
                    .message(MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>").build())
                    .header(resource);
            }
        };
        
        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someHeaderData".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("otherHeaderData".getBytes())).once();
        replay(resource);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 2);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getMessageHeaderData(), "someHeaderData");
        
        action = ((SendMessageAction)builder.testCase().getActions().get(1));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getMessageHeaderData(), "otherHeaderData");
    }
    
    @Test
    public void testReceiveBuilderExtractFromPayload() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getxPathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getxPathExpressions().containsKey("/TestRequest/Message/@lang"));
    }
    
    @Test
    public void testReceiveBuilderExtractFromHeader() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                send(messageSender)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id");
            }
        };
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), SendMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageSender(), messageSender);
        
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));
    }
}
