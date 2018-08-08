
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
