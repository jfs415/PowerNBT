package me.dpohvar.powernbt.command.action;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.nbt.NBTContainer;
import me.dpohvar.powernbt.utils.Caller;
import me.dpohvar.powernbt.utils.query.NBTQuery;

public class ActionMoveLast extends Action {

	private final Caller caller;
	private final Argument arg1;
	private final Argument arg2;

	public ActionMoveLast(Caller caller, String o1, String q1, String o2, String q2) {
		this.caller = caller;
		this.arg1 = new Argument(caller, o1, q1);
		this.arg2 = new Argument(caller, o2, q2);
	}

	@Override
	public void execute() throws Exception {
		if (arg1.needPrepare()) {
			arg1.prepare(this, null, null);
			return;
		}
		NBTContainer container1 = arg1.getContainer();
		NBTQuery query1 = arg1.getQuery();
		if (arg2.needPrepare()) {
			arg2.prepare(this, container1, query1);
			return;
		}
		NBTQuery query2 = arg2.getQuery();
		NBTContainer container2 = arg2.getContainer();
		Object base = arg1.getContainer().getCustomTag(arg1.getQuery());
		if (base == null) {
			throw new RuntimeException(PowerNBT.plugin.translate("error_null"));
		}

		try {
			container2.setCustomTag(query2, base);
			arg1.getContainer().removeTag(arg1.getQuery());
			caller.sendValue(PowerNBT.plugin.translate("success_move"), base, false, false);
		} catch (Exception e) {
			throw new RuntimeException(PowerNBT.plugin.translate("fail_move", query2.toString()), e);
		}
	}

}
