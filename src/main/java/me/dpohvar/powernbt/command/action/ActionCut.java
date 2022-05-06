package me.dpohvar.powernbt.command.action;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.nbt.NBTContainer;
import me.dpohvar.powernbt.utils.Caller;
import me.dpohvar.powernbt.utils.query.NBTQuery;

public class ActionCut extends Action {

	private final Caller caller;
	private final Argument arg;

	public ActionCut(Caller caller, String o1, String q1) {
		this.caller = caller;
		this.arg = new Argument(caller, o1, q1);
	}

	@Override
	public void execute() throws Exception {
		if (arg.needPrepare()) {
			arg.prepare(this, null, null);
			return;
		}
		NBTContainer container = arg.getContainer();
		NBTQuery query = arg.getQuery();
		Object base = container.getCustomTag(query);
		if (base == null) {
			throw new RuntimeException(PowerNBT.plugin.translate("error_null"));
		}
		caller.setCustomTag(base);
		caller.sendValue(PowerNBT.plugin.translate("success_cut"), base, false, false);
		arg.getContainer().removeTag(arg.getQuery());
	}

}
