/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.statreport;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.AlertNode;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.ascan.ExtensionActiveScan;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * An example ZAP extension which adds a top level menu item, a pop up menu item and a status panel.
 *
 * <p>{@link ExtensionAdaptor} classes are the main entry point for adding/loading functionalities
 * provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionStatReport extends ExtensionAdaptor {

    public static final String NAME = "ExtensionStatReport";
    protected static final String PREFIX = "statreport";

    private static final String FIELD_MENU_TITLE = PREFIX + ".topmenu.title";

    private ZapMenuItem menuItem;
    private StatReportDialog statReportDialog;
    private StatReportParam statReportParam;

    private StatReportAPI api;

    private static final Logger LOGGER = LogManager.getLogger(ExtensionStatReport.class);

    public ExtensionStatReport() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        this.api = new StatReportAPI();
        extensionHook.addApiImplementor(this.api);

        if (hasView()) {
            extensionHook.getHookMenu().addReportMenuItem(getReportMenu());
        }
    }

    @Override
    public boolean canUnload() {
        return true;
    }

    @Override
    public void unload() {
        super.unload();
    }

    private ZapMenuItem getReportMenu() {
        if (menuItem == null) {
            menuItem = new ZapMenuItem(FIELD_MENU_TITLE);
            menuItem.addActionListener(
                    e -> {
                        getReportDialog().init();
                        getReportDialog().setVisible(true);
                    });
        }
        return menuItem;
    }

    private StatReportDialog getReportDialog() {
        if (statReportDialog == null) {
            statReportDialog = new StatReportDialog(this,View.getSingleton().getMainFrame());
        }
        return statReportDialog;
    }

    public StatReportParam getReportParam(){
        if(statReportParam == null){
            statReportParam = new StatReportParam("","");
        }
        return statReportParam;
    }

    public void generateReport(StatReportParam param) throws IOException {
        System.out.println("generateReport");
        System.out.println(param.getReportFileEncode());
        System.out.println(param.getReportFile());
        System.out.println(param.getReportContext());

        final var scan = (ExtensionActiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionActiveScan.NAME);
        final var policyList = scan.getPolicyManager().getAttackScanPolicy().getPluginFactory().getAllPlugin();

        final var alert = Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
        Method treeModelMethod = null;
        try {
            treeModelMethod = alert.getClass().getDeclaredMethod("getTreeModel");
            treeModelMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        DefaultTreeModel treeModel = null;
        try {
            treeModel = (DefaultTreeModel) treeModelMethod.invoke(alert);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        final var root = (AlertNode) treeModel.getRoot();

        final var dataList = new ArrayList<StatReportStatData>();
        for (Context c : param.getReportContext()) {
            System.out.println(c.getName());
            for (SiteNode node : c.getNodesInContextFromSiteTree()) {
                final var history = node.getHistoryReference();
                final var requestData = new StatReportRequestData(
                        history.getURI().toString(),
                        history.getURI().getHost(),
                        history.getURI().getPort(),
                        history.getURI().getPath(),
                        history.getMethod(),
                        history.getURI().getQuery(),
                        history.getRequestBody());
                final var dataForNode = new StatReportStatData(c.getName(),requestData,policyList);

                System.out.println("history:"+history.getURI().toString());

                // Loop for alarts
                if (root.getChildCount() > 0) {
                    AlertNode child = (AlertNode) root.getFirstChild();
                    while (child != null) {
                        System.out.println("alart:"+child.getUserObject().getUri());

                        final var alartRequestUri = URI.create(child.getUserObject().getUri());
                        final var alartRequestData = new StatReportRequestData(
                                alartRequestUri.toString(),
                                alartRequestUri.getHost(),
                                alartRequestUri.getPort(),
                                alartRequestUri.getPath(),
                                child.getUserObject().getMethod(),
                                alartRequestUri.getQuery(),
                                child.getUserObject().getPostData());

                        // Match history & alart URL
                        if (requestData.equals(alartRequestData)){
                            System.out.println(child.getUserObject().getName());
                            boolean matchAnyPolicy = false;
                            for (Plugin p : policyList){
                                if (p.getId() == child.getUserObject().getPluginId()){
                                    matchAnyPolicy = true;
                                    dataForNode.inclementAlartCount(p.getName());
                                    break;
                                }
                            }
                            if (!matchAnyPolicy){
                                dataForNode.inclementAlartCount("Others");
                            }
                        }
                        child = (AlertNode) root.getChildAfter(child);
                    }
                }

                for(String key : dataForNode.getAlartCount().keySet()){
                    System.out.println(key + ":" + dataForNode.getAlartCount().get(key));
                }
                dataList.add(dataForNode);
            }
        }
        System.out.println("size:" + String.valueOf(dataList.size()) );

        if (dataList.size() == 0){
            return;
        }

        final var LINE_FEED_CODE = System.getProperty("line.separator");
        final var DELIMITER_COMMA = ",";
        try(final var writer = new PrintWriter(param.getReportFile(), param.getReportFileEncode())){
            // header
            writer.print("CONTEXT,METHOD,HOST,PORT,PATH,QUERY/BODY");
            writer.print(DELIMITER_COMMA);

            for (String key : dataList.get(0).getAlartCount().keySet()){
                writer.print(key);
                writer.print(DELIMITER_COMMA);
            }
            writer.print(LINE_FEED_CODE);

            //
            for(StatReportStatData d : dataList){
                writer.print(d.getRequestContext());
                writer.print(DELIMITER_COMMA);
                writer.print(d.getRequestData().getRequestMethod());
                writer.print(DELIMITER_COMMA);
                writer.print(d.getRequestData().getRequestHost());
                writer.print(DELIMITER_COMMA);
                writer.print(d.getRequestData().getRequestPort());
                writer.print(DELIMITER_COMMA);
                writer.print(d.getRequestData().getRequestPath());
                writer.print(DELIMITER_COMMA);
                writer.print("query: "+d.getRequestData().getRequestQuery()+";body: "+d.getRequestData().getRequestBody());
                writer.print(DELIMITER_COMMA);
                for (String key : d.getAlartCount().keySet()){
                    System.out.println("csv:"+key);
                    writer.print(d.getAlartCount().get(key));
                    writer.print(DELIMITER_COMMA);
                }
                writer.print(LINE_FEED_CODE);
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
