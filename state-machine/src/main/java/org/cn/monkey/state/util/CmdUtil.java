package org.cn.monkey.state.util;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import org.cn.monkey.cmd.proto.Command;
import org.cn.monkey.common.utils.ResultCode;

public class CmdUtil {
    private CmdUtil() {
    }

    private static Command.PackageGroup packageGroup(Command.Package... packages) {
        Command.PackageGroup.Builder builder = Command.PackageGroup.newBuilder();
        for (Command.Package pkg : packages) {
            if (pkg != null) {
                builder.addPackages(pkg);
            }
        }
        return builder.build();
    }

    private static Command.ResultMessage resultMsg(int resultCode, String msg) {
        Command.ResultMessage.Builder builder = Command.ResultMessage.newBuilder().setCode(resultCode);
        if (!Strings.isNullOrEmpty(msg)) {
            builder.setMsg(msg);
        }
        return builder.build();
    }

    private static Command.Package pkg(int resultCode, String msg, int cmdType, ByteString content) {
        Command.Package.Builder builder = Command.Package.newBuilder().setResultMsg(resultMsg(resultCode, msg));
        if (cmdType > 0) {
            builder.setCmdType(cmdType);
        }
        if (content != null) {
            builder.setContent(content);
        }
        return builder.build();
    }

    public static Command.PackageGroup errorMsg(String msg, int cmdType) {
        return packageGroup(pkg(ResultCode.ERROR, msg, cmdType, null));
    }
}
