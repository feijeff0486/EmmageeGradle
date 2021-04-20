package com.netease.qa.emmagee.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Keep;
import android.support.v4.content.FileProvider;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Jeff
 * @describe
 * @date 2019/1/21.
 */
public final class ShellUtils {

    private static final String LINE_SEP = System.getProperty("line.separator");

    private ShellUtils() {
        throw new CannotCreateException(getClass());
    }

    /**
     * Execute the command.
     *
     * @param command  The command.
     * @param isRooted True to use root, false otherwise.
     * @return the single {@link CommandResult} instance
     */
    public static CommandResult execCmd(final String command, final boolean isRooted) {
        return execCmd(new String[]{command}, isRooted, true);
    }

    /**
     * Execute the command.
     *
     * @param commands The commands.
     * @param isRooted True to use root, false otherwise.
     * @return the single {@link CommandResult} instance
     */
    public static CommandResult execCmd(final List<String> commands, final boolean isRooted) {
        return execCmd(commands == null ? null : commands.toArray(new String[]{}), isRooted, true);
    }

    /**
     * Execute the command.
     *
     * @param commands The commands.
     * @param isRooted True to use root, false otherwise.
     * @return the single {@link CommandResult} instance
     */
    public static CommandResult execCmd(final String[] commands, final boolean isRooted) {
        return execCmd(commands, isRooted, true);
    }

    /**
     * Execute the command.
     *
     * @param command         The command.
     * @param isRooted        True to use root, false otherwise.
     * @param isNeedResultMsg True to return the message of result, false otherwise.
     * @return the single {@link CommandResult} instance
     */
    public static CommandResult execCmd(final String command,
                                        final boolean isRooted,
                                        final boolean isNeedResultMsg) {
        return execCmd(new String[]{command}, isRooted, isNeedResultMsg);
    }

    /**
     * Execute the command.
     *
     * @param commands        The commands.
     * @param isRooted        True to use root, false otherwise.
     * @param isNeedResultMsg True to return the message of result, false otherwise.
     * @return the single {@link CommandResult} instance
     */
    public static CommandResult execCmd(final List<String> commands,
                                        final boolean isRooted,
                                        final boolean isNeedResultMsg) {
        return execCmd(commands == null ? null : commands.toArray(new String[]{}),
                isRooted,
                isNeedResultMsg);
    }

    /**
     * Execute the command.
     *
     * @param commands        The commands.
     * @param isRooted        True to use root, false otherwise.
     * @param isNeedResultMsg True to return the message of result, false otherwise.
     * @return the single {@link CommandResult} instance
     */
    public static CommandResult execCmd(final String[] commands,
                                        final boolean isRooted,
                                        final boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, "", "");
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRooted ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) continue;
                os.write(command.getBytes());
                os.writeBytes(LINE_SEP);
                os.flush();
            }
            os.writeBytes("exit" + LINE_SEP);
            os.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
                );
                errorResult = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8)
                );
                String line;
                if ((line = successResult.readLine()) != null) {
                    successMsg.append(line);
                    while ((line = successResult.readLine()) != null) {
                        successMsg.append(LINE_SEP).append(line);
                    }
                }
                if ((line = errorResult.readLine()) != null) {
                    errorMsg.append(line);
                    while ((line = errorResult.readLine()) != null) {
                        errorMsg.append(LINE_SEP).append(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(os, successResult, errorResult);
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(
                result,
                successMsg == null ? "" : successMsg.toString(),
                errorMsg == null ? "" : errorMsg.toString()
        );
    }

    /**
     * The result of command.
     */
    @Keep
    public static class CommandResult implements Serializable {
        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(final int result, final String successMsg, final String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "result: " + result + "\n" +
                    "successMsg: " + successMsg + "\n" +
                    "errorMsg: " + errorMsg;
        }
    }

    /**
     * 安装apk
     */
    // TODO: 2020/3/28 update authority
    public static int installAPK(Context context, File apkFile) {

        if (null == apkFile) {
            return -1;
        }
        try {
            setPermission(apkFile.getAbsolutePath());
            // 安装APK
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String type = "application/vnd.android.package-archive";
            if (Build.VERSION.SDK_INT >= 24) {
                Uri uriForFile = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + "authority",
                        apkFile
                );
                intent.setDataAndType(uriForFile, type);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), type);
            }

            context.startActivity(intent);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 修改文件权限
     */
    private static void setPermission(String absolutePath) {
        try {
            String command = "chmod " + "777" + " " + absolutePath;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}