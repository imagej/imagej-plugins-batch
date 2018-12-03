/*-
 * #%L
 * Batch Processor Plugins for ImageJ
 * %%
 * Copyright (C) 2018 Friedrich Miescher Institute for Biomedical Research, Basel (Switzerland)
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imagej.batch.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.scif.SCIFIOService;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.imagej.ImageJService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.batch.BatchService;
import org.scijava.batch.ModuleBatchProcessor;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.script.ScriptInfo;
import org.scijava.service.SciJavaService;
import org.scijava.table.Table;

public class DatasetBatchInputTest {

	private Context context;

	@Before
	public void initialize() {
		context = new Context(SciJavaService.class, ImageJService.class, SCIFIOService.class);
	}

	@After
	public void disposeContext() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Test
	public void testContext() {
		final BatchService batchService = context
				.getService(BatchService.class);
		assertNotNull(batchService);
	}

	@Test
	public void testBatchableDatasetInputs() {
		String script = "" //
			+ "#@ ImgPlus imgInput\n" //
			+ "#@ File fileInput\n" //
			// + "#@ String stringInput\n" // almost everything can be converted to String :(
			+ "#@ Integer integerInput\n" //
			+ "#@ Double doubleInput\n" //
			+ "";
		ScriptInfo scriptInfo = createInfo(script);
		BatchService batchService = context.getService(BatchService.class);
		List<ModuleItem<?>> compatibleInputs = batchService.batchableInputs(
			scriptInfo);
		assertEquals("Wrong number of batchable inputs", 2, compatibleInputs
			.size());
	}
	
	@Test
	public void testDatasetBatchProcessing() {
		File[] fileArray = new File[3];
		fileArray[0] = new File("8bit-signed&pixelType=int8&axes=X,Y&lengths=10,20.fake");
		fileArray[1] = new File("8bit-signed&pixelType=int8&axes=X,Y&lengths=30,40.fake");
		fileArray[2] = new File("8bit-signed&pixelType=int8&axes=X,Y&lengths=50,60.fake");

		String script = "" //
				+ "#@ ImgPlus imgInput\n" //
				+ "#@output width\n" //
				+ "#@output height\n" //
				+ "\n" //
				+ "width = imgInput.dimension(0)\n" //
				+ "height = imgInput.dimension(1)\n" //
				+ "";
		
		ScriptInfo scriptInfo = createInfo(script);

		HashMap<String, Object> inputMap = new HashMap<>();
		inputMap.put("moduleInfo", scriptInfo);
		inputMap.put("inputChoice", "imgInput");
		inputMap.put("inputFileList", fileArray);
		//inputMap.put("outputFolder", null);
		ModuleService moduleService = context.getService(ModuleService.class);
		CommandService commandService = context
				.getService(CommandService.class);
		CommandInfo commandInfo = commandService
				.getCommand(ModuleBatchProcessor.class);
		Module module = moduleService.createModule(commandInfo);
		try {
			module = moduleService.run(module, true, inputMap).get();
		} catch (InterruptedException | ExecutionException exc) {
			exc.printStackTrace();
		}
		Table<?, ?> outputs = (Table<?, ?>) module.getOutput("outputTable");
		
		assertEquals("Image 1 width", new Long(10), outputs.get(0, 0));
		assertEquals("Image 1 height", new Long(20), outputs.get(1, 0));
		assertEquals("Image 2 width", new Long(30), outputs.get(0, 1));
		assertEquals("Image 2 height", new Long(40), outputs.get(1, 1));
		assertEquals("Image 3 width", new Long(50), outputs.get(0, 2));
		assertEquals("Image 3 height", new Long(60), outputs.get(1, 2));
	}

	/* --- Helper methods --- */

	private ScriptInfo createInfo(String script) {
		StringReader scriptReader = new StringReader(script);
		return new ScriptInfo(context, "Foo.groovy", scriptReader);
	}

}
