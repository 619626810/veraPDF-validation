package org.verapdf.features.gf.objects;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.FeatureExtractionResult;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.features.FeaturesData;
import org.verapdf.features.IFeaturesObject;
import org.verapdf.features.gf.tools.ColorComponent;
import org.verapdf.features.gf.tools.GFCreateNodeHelper;
import org.verapdf.features.tools.ErrorsHelper;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.pd.PDOutlineDictionary;
import org.verapdf.pd.PDOutlineItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Feature object for outlines
 *
 * @author Maksim Bezrukov
 */
public class GFOutlinesFeaturesObject implements IFeaturesObject {

	private PDOutlineDictionary outline;

	/**
	 * Constructs new OutputIntent Feature Object
	 *
	 * @param outline class represents outlines object
	 */
	public GFOutlinesFeaturesObject(PDOutlineDictionary outline) {
		this.outline = outline;
	}

	/**
	 * @return OUTLINES instance of the FeatureObjectType enumeration
	 */
	@Override
	public FeatureObjectType getType() {
		return FeatureObjectType.OUTLINES;
	}

	/**
	 * Reports featurereport into collection
	 *
	 * @param collection collection for feature report
	 * @return FeatureTreeNode class which represents a root node of the
	 * constructed collection tree
	 * @throws FeatureParsingException occurs when wrong features tree node constructs
	 */
	@Override
	public FeatureTreeNode reportFeatures(FeatureExtractionResult collection)
			throws FeatureParsingException {
		if (outline != null) {
			FeatureTreeNode root = FeatureTreeNode.createRootNode("outlines");

			Set<PDOutlineItem> items = new HashSet<>();
			for (PDOutlineItem item : getChildren(outline)) {
				if (!items.contains(item)) {
					createItem(item, root, collection, items);
				}
			}

			collection
					.addNewFeatureTree(FeatureObjectType.OUTLINES, root);
			return root;
		}
		return null;
	}

	/**
	 * @return null
	 */
	@Override
	public FeaturesData getData() {
		return null;
	}

	private static void createItem(PDOutlineItem item, FeatureTreeNode root,
								   FeatureExtractionResult collection, Set<PDOutlineItem> items) throws FeatureParsingException {
		if (item != null) {
			items.add(item);
			FeatureTreeNode itemNode = root.addChild("outline");

			GFCreateNodeHelper.addNotEmptyNode("title", item.getTitle(), itemNode);

			FeatureTreeNode color = itemNode.addChild("color");
			double[] clr = item.getColor();
			if (clr != null) {
				color.setAttributes(ColorComponent.RGB_COMPONENTS.createAttributesMap(clr));
			} else {
				ErrorsHelper.addErrorIntoCollection(collection,
						color,
						"Color must be in rgb form");
			}


			FeatureTreeNode style = itemNode.addChild("style");
			style.setAttribute("italic", String.valueOf(item.isItalic()));
			style.setAttribute("bold", String.valueOf(item.isBold()));

			for (PDOutlineItem child : getChildren(item)) {
				if (!items.contains(child)) {
					createItem(child, itemNode, collection, items);
				}
			}
		}
	}


	public static List<PDOutlineItem> getChildren(PDOutlineDictionary dictionary) {
		List<PDOutlineItem> res = new ArrayList<>();
		PDOutlineItem curr = dictionary.getFirst();
		while (curr != null) {
			res.add(curr);
			curr = curr.getNext();
		}
		return res;
	}
}