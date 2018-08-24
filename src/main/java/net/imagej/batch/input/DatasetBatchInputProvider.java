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

import io.scif.services.DatasetIOService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.imagej.Dataset;

import org.scijava.ItemVisibility;
import org.scijava.batch.input.BatchInput;
import org.scijava.batch.input.BatchInputProvider;
import org.scijava.convert.ConversionRequest;
import org.scijava.convert.ConvertService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileListWidget;

@Plugin(type = BatchInputProvider.class)
public class DatasetBatchInputProvider extends AbstractHandlerPlugin<BatchInput> implements
	BatchInputProvider<File>
{
	@Parameter
	private ConvertService convertService;

	@Parameter
	private DatasetIOService ioService;

	@Override
	public boolean supports(BatchInput input) {
		return canProvide(input.moduleItem());
	}

	@Override
	public boolean canProvide(ModuleItem<?> item) {
		if (item.getVisibility() == ItemVisibility.MESSAGE) {
			return false;
		}
		return convertService.supports(new ConversionRequest(Dataset.class, item.getType()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void populateInput(Module module, ModuleItem<?> moduleItem,
		File inputObject)
	{
		try {
			Dataset dataset = ioService.open(inputObject.getAbsolutePath());
			Object converted = convertService.convert(dataset, moduleItem.getType());
			((ModuleItem<Object>) moduleItem).setValue(module, converted);
		}
		catch (IOException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
	}

	@Override
	public String getTargetWidgetStyle(ModuleItem<?> item) {
		ArrayList<String> targetStyles = new ArrayList<>();

		targetStyles.add(FileListWidget.FILES_ONLY);

		// TODO extensions for images
		String widgetStyle = item.getWidgetStyle();
		if (widgetStyle != null) {
			String[] styles = widgetStyle.trim().split("\\s*,\\s*");
			for (String s : styles) {
				if (s.startsWith("extensions")) { // TODO: use new constant from FileListWidget
					targetStyles.add(s);
				}
			}			
		}
		return String.join(",", targetStyles);
	}

}
