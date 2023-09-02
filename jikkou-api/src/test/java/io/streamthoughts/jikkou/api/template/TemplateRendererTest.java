/*
 * Copyright 2022 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.template;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.util.Map;
//
public class TemplateRendererTest {
//
//    TemplateRenderer templateRenderer = new TemplateRenderer();
//
//    @Test
//    public void should_render_template_given_simple_key() {
//
//        var result = templateRenderer.render(
//                "{{ labels.my_var }}",
//                TemplateBindings.defaults().withLabels(Map.of("my_var", "test"))
//        );
//        Assertions.assertEquals("test", result);
//    }
//
//    @Test
//    public void should_render_template_given_hierarchical_keys() {
//        TemplateBindings binding = TemplateBindings.defaults().withLabels(Map.of(
//                "my.var1", "val1",
//                "my", Map.of("var2", "val2")
//        ));
//
//        Assertions.assertEquals("val1",
//                templateRenderer.render("{{ labels.my.var1 }}", binding));
//        Assertions.assertEquals("val1",
//                templateRenderer.render("{{ labels['my.var1'] }}", binding));
//        Assertions.assertEquals("val1",
//                templateRenderer.render("{{ labels['my'].var1 }}", binding));
//
//        Assertions.assertEquals("val2",
//                templateRenderer.render("{{ labels.my.var2 }}", binding));
//        Assertions.assertEquals("val2",
//                templateRenderer.render("{{ labels['my.var2'] }}", binding));
//        Assertions.assertEquals("val2",
//                templateRenderer.render("{{ labels['my'].var2 }}", binding));
//    }
}