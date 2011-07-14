/**
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

package net.ftlines.cdigen.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes( { 
	MetaProcessor.DEPENDENT,
	MetaProcessor.APPLICATION_SCOPED,
	MetaProcessor.SESSION_SCOPED,
	MetaProcessor.CONVERSATION_SCOPED,
	MetaProcessor.REQUEST_SCOPED,
	MetaProcessor.INTERCEPTOR })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MetaProcessor implements Processor {
	
	public static final String DEPENDENT = "javax.enterprise.context.Dependent";
	public static final String APPLICATION_SCOPED = "javax.enterprise.context.ApplicationScoped";
	public static final String SESSION_SCOPED = "javax.enterprise.context.SessionScoped";
	public static final String CONVERSATION_SCOPED = "javax.enterprise.context.ConversationScoped";
	public static final String REQUEST_SCOPED = "javax.enterprise.context.RequestScoped";
	public static final String INTERCEPTOR = "javax.interceptor.Interceptor";
	
	private ProcessingEnvironment environment;

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment round) {

		Set<TypeElement> types = new HashSet<TypeElement>();
		for (TypeElement annotation : annotations) {
			for (Element element : (Set<? extends Element>) round.getElementsAnnotatedWith(annotation)) {
				while (element != null) {
					if (ElementKind.CLASS.equals(element.getKind()))
						types.add((TypeElement)element);
					element = element.getEnclosingElement();
				}
			}
		}

		for (TypeElement type : types)
			try {
				FileObject file; 
				String className = "";
				TypeElement top = type;
				String pkg = "";
				while (top != null)
				{
					className = top.getSimpleName() + (className.isEmpty() ? "" : ('$' + className));
					if (top.getEnclosingElement() instanceof TypeElement)
						top = (TypeElement) top.getEnclosingElement();
					else if (top.getEnclosingElement() instanceof PackageElement)
					{
						pkg = ((PackageElement)top.getEnclosingElement()).getQualifiedName().toString();
						top = null;
					}
				}
				file = environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, pkg, className + ".component", type);
				Writer fw = file.openWriter();
				PrintWriter pw = new PrintWriter(fw);
				pw.print("Component file created on " + new Date());
				pw.flush();
				fw.close();
				
			} catch (IOException e) {
				environment.getMessager().printMessage(
						Kind.ERROR,
						"Could not write source for: " + type.getQualifiedName()
								+ ": " + e.getMessage());
			}
		return true;
	}



	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return new HashSet<String>(Arrays.asList(
				INTERCEPTOR,
				REQUEST_SCOPED,
				CONVERSATION_SCOPED,
				SESSION_SCOPED,
				APPLICATION_SCOPED,
				DEPENDENT
				));
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_6;
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		environment = processingEnv;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element,
			AnnotationMirror annotation, ExecutableElement member,
			String userText) {
		return Collections.emptyList();
	}
}
