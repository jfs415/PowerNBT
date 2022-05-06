package me.dpohvar.powernbt.command.action;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.nbt.NBTContainer;
import me.dpohvar.powernbt.utils.Caller;
import me.dpohvar.powernbt.utils.query.NBTQuery;
import me.dpohvar.powernbt.utils.query.QSelector;

import java.util.List;

public class ActionRename extends Action {

	private final Caller caller;
	private final Argument arg1;
	private final String name;
	private final NBTQuery query2;

	public ActionRename(Caller caller, String o1, String q1, String name) {
		this.caller = caller;
		this.arg1 = new Argument(caller, o1, q1);
		this.name = name;
		this.query2 = NBTQuery.fromString(name);
	}

	@Override
	public void execute() throws Exception {
		if (arg1.needPrepare()) {
			arg1.prepare(this, null, null);
			return;
		}
		NBTContainer container = arg1.getContainer();
		NBTQuery query = arg1.getQuery();
		List<QSelector> v = query.getParent().getSelectors();
		v.addAll(query2.getSelectors());
		NBTQuery newQuery = new NBTQuery(v);
		Object root = container.getTag();
		Object base = query.get(root);
		if (base == null) {
			caller.send(PowerNBT.plugin.translate("fail_rename"));
		} else {
			root = query.remove(root);
			root = newQuery.set(root, base);
			container.setCustomTag(root);
			caller.sendValue(PowerNBT.plugin.translate("success_rename", name), base, false, false);
		}
	}

}














