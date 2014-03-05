package odra.sbql.ast.declarations;

import java.util.Vector;

/**
 * @author ks
 * 
 */
public class SequenceViewBodySection {

	Vector<ViewBodySection> v = new Vector<ViewBodySection>();

	public SequenceViewBodySection() {

	}

	public SequenceViewBodySection(SequenceViewBodySection list,
			ViewBodySection section) {
		v = (Vector<ViewBodySection>) list.v.clone();
		v.add(section);

	}

	public ViewBodySection[] toArray() {
		return (ViewBodySection[]) v.toArray(new ViewBodySection[v.size()]);
	}
}
