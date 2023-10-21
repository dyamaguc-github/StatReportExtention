package org.zaproxy.addon.statreport;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.StandardFieldsDialog;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

import static org.zaproxy.addon.statreport.ExtensionStatReport.PREFIX;

public class StatReportDialog extends StandardFieldsDialog {

    private static final Logger LOGGER = LogManager.getLogger(StatReportDialog.class);

    private JButton[] extraButtons = null;
    private ExtensionStatReport extension = null;
    private static final long serialVersionUID = 1L;
    private JList<Context> contextsSelector;
    private DefaultListModel<Context> contextsModel;

    private static final String FIELD_PREFIX = PREFIX + ".dialog";
    private static final String FIELD_REPORT_TITLE = FIELD_PREFIX + ".title";
    private static final String FIELD_REPORT_SAVE = FIELD_PREFIX + ".button.save";
    private static final String FIELD_REPORT_RESET = FIELD_PREFIX + ".button.reset";
    private static final String FIELD_REPORT_FILE = FIELD_PREFIX + ".field.reportfile";
    private static final String FIELD_REPORT_FILE_DEFAULT = FIELD_PREFIX + ".field.reportfile.default";
    private static final String FIELD_REPORT_FILE_ENCODE = FIELD_PREFIX + ".field.reportfile.encode";
    private static final String FIELD_REPORT_FILE_ENCODE_TYPE = FIELD_PREFIX + ".field.reportfile.encode.type";
    private static final String FIELD_REPORT_CONTEXT = FIELD_PREFIX + ".field.context";

    private static final String FIELD_REPORT_ERROR_NOCONTEXT = FIELD_PREFIX + ".error.nocontext";
    private static final String FIELD_REPORT_ERROR_EMPTYCONTEXT = FIELD_PREFIX + ".error.emptycontext";
    private static final String FIELD_REPORT_ERROR_NOREPORTFILE = FIELD_PREFIX + ".error.noreportfile";
    private static final String FIELD_REPORT_ERROR_NOFILEENCODE = FIELD_PREFIX + ".error.nofileencode";
    private static final String FIELD_REPORT_ERROR_DIRPERMS = FIELD_PREFIX + ".error.dirperms";
    private static final String FIELD_REPORT_ERROR_FILEPERMS = FIELD_PREFIX + ".error.fileperms";

    private static final String FIELD_REPORT_LOG_PREFIX = PREFIX + ".log.prefix";
    private static final String FIELD_REPORT_LOG_INFO_GENERATEREPORT = PREFIX + ".log.info.generatereport";
    private static final String FIELD_REPORT_LOG_ERROR_GENERATEREPORT = PREFIX + ".log.error.generatereport";

    public StatReportDialog(ExtensionStatReport ext, Frame owner) {
        super(owner,
                FIELD_REPORT_TITLE,
                DisplayUtils.getScaledDimension(600, 300));
        this.extension = ext;
        reset(true);
    }

    public void init() {
        this.removeAllFields();
        this.contextsSelector = null;

        this.addCustomComponent(FIELD_REPORT_CONTEXT,
                getNewJScrollPane(getContextsSelector(), 400, 50));

        this.addFileSelectField(
                FIELD_REPORT_FILE,
                new File( System.getProperty("user.home")+
                        File.separator+
                        Constant.messages.getString(FIELD_REPORT_FILE_DEFAULT) ),
                JFileChooser.FILES_ONLY,
                null);

        this.addTextField(FIELD_REPORT_FILE_ENCODE, Constant.messages.getString(FIELD_REPORT_FILE_ENCODE_TYPE));

        this.pack();
    }

    private JScrollPane getNewJScrollPane(Component view, int width, int height) {
        JScrollPane pane = new JScrollPane(view);
        pane.setPreferredSize(DisplayUtils.getScaledDimension(width, height));
        pane.setMinimumSize((DisplayUtils.getScaledDimension(width, height)));
        return pane;
    }

    private DefaultListModel<Context> getContextsModel() {
        if (contextsModel == null) {
            contextsModel = new DefaultListModel<>();
            for (Context context : Model.getSingleton().getSession().getContexts()) {
                contextsModel.addElement(context);
            }
        }
        return contextsModel;
    }

    private JList<Context> getContextsSelector() {
        if (contextsSelector == null) {
            contextsSelector = new JList<>(getContextsModel());
            contextsSelector.setCellRenderer(
                    new DefaultListCellRenderer() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public Component getListCellRendererComponent(
                                JList<?> list,
                                Object value,
                                int index,
                                boolean isSelected,
                                boolean cellHasFocus) {
                            JLabel label =
                                    (JLabel)
                                            super.getListCellRendererComponent(
                                                    list, value, index, isSelected, cellHasFocus);
                            if (value instanceof Context) {
                                label.setText(((Context) value).getName());
                            }
                            return label;
                        }
                    });
        }
        return contextsSelector;
    }

    private void reset(boolean refreshUi) {
        if (refreshUi) {
            init();
            repaint();
        }
    }

    @Override
    public String getHelpIndex() {
        return "reports";
    }

    @Override
    public String getSaveButtonText() {
        return Constant.messages.getString(FIELD_REPORT_SAVE);
    }

    @Override
    public JButton[] getExtraButtons() {
        if (extraButtons == null) {
            JButton resetButton =
                    new JButton(Constant.messages.getString(FIELD_REPORT_RESET));
            resetButton.addActionListener(e -> reset(true));

            extraButtons = new JButton[] {resetButton};
        }
        return extraButtons;
    }

    @Override
    public void setVisible(boolean show) {
        super.setVisible(show);
    }

    @Override
    public String validateFields() {
        StatReportParam param = extension.getReportParam();
        param.setReportFile(this.getStringValue(FIELD_REPORT_FILE));
        param.setReportFileEncode(this.getStringValue(FIELD_REPORT_FILE_ENCODE));
        param.setReportContext(this.getContextsSelector().getSelectedValuesList());

        if (StringUtils.isEmpty(param.getReportFile())){
            return Constant.messages.getString(FIELD_REPORT_ERROR_NOREPORTFILE);
        }

        if (StringUtils.isEmpty(param.getReportFileEncode())){
            return Constant.messages.getString(FIELD_REPORT_ERROR_NOFILEENCODE);
        }

        final var f = new File(param.getReportFile());
        if (!f.exists()){
            if (!f.getParentFile().canWrite()){
                Constant.messages.getString(FIELD_REPORT_ERROR_DIRPERMS, f.getParentFile().getAbsolutePath());
            }
        }else if(!f.canWrite()){
            Constant.messages.getString(FIELD_REPORT_ERROR_FILEPERMS, f.getAbsolutePath());
        }

        if ( param.getReportContext().size() == 0 ){
            return Constant.messages.getString(FIELD_REPORT_ERROR_NOCONTEXT);
        }
        int nodes = 0;
        for (Context c : param.getReportContext()) {
            nodes += c.getNodesInContextFromSiteTree().size();
        }
        if ( nodes == 0 ){
            return Constant.messages.getString(FIELD_REPORT_ERROR_EMPTYCONTEXT);
        }

        return null;
    }

    @Override
    public void save() {
        StatReportParam param = extension.getReportParam();
        param.setReportFile(this.getStringValue(FIELD_REPORT_FILE));
        param.setReportFileEncode(this.getStringValue(FIELD_REPORT_FILE_ENCODE));
        param.setReportContext(this.getContextsSelector().getSelectedValuesList());
        try {
            this.extension.generateReport(param);
        } catch (IOException e) {
            this.displayOutputPanel(Constant.messages.getString(FIELD_REPORT_LOG_ERROR_GENERATEREPORT, e.getMessage()));
            LOGGER.error(Constant.messages.getString(FIELD_REPORT_LOG_ERROR_GENERATEREPORT,e.getMessage()));
        }
        this.displayOutputPanel(Constant.messages.getString(FIELD_REPORT_LOG_INFO_GENERATEREPORT, param.getReportFile()));
    }

    private void displayOutputPanel(String msg){
        View.getSingleton().getOutputPanel().append(
                Constant.messages.getString(FIELD_REPORT_LOG_PREFIX) + msg + "\n");
    }
}