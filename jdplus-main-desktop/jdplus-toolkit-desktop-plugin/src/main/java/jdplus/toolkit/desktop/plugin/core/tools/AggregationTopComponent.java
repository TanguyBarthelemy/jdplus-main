/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.tools;

import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection.TsUpdateMode;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.desktop.plugin.components.JTsChart;
import jdplus.toolkit.desktop.plugin.components.JTsTable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import java.awt.BorderLayout;
import java.util.Optional;
import javax.swing.JSplitPane;

import nbbrd.design.ClassNameConstant;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//demetra.desktop.core.tools//Aggregation//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "AggregationTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = AggregationTopComponent.ID)
@ActionReference(path = "Menu/Tools", position = 332)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_AggregationAction")
@Messages({
    "CTL_AggregationAction=Aggregation",
    "CTL_AggregationTopComponent=Aggregation Window",
    "HINT_AggregationTopComponent=This is an Aggregation window"
})
public final class AggregationTopComponent extends TopComponent {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.tools.AggregationTopComponent";

    private final JSplitPane mainPane;
    private final JTsTable inputList;
    private final JTsChart aggChart;

    public AggregationTopComponent() {
        initComponents();
        setName(Bundle.CTL_AggregationTopComponent());
        setToolTipText(Bundle.HINT_AggregationTopComponent());
        inputList = new JTsTable();
        initList();
        aggChart = new JTsChart();
        aggChart.setTsUpdateMode(TsUpdateMode.None);
        mainPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputList, aggChart);

        setLayout(new BorderLayout());
        add(mainPane, BorderLayout.CENTER);
        mainPane.setDividerLocation(.5);
        mainPane.setResizeWeight(.5);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void initList() {
        inputList.addPropertyChangeListener(JTsTable.TS_COLLECTION_PROPERTY, evt -> {
            Optional<Ts> sum = inputList.getTsCollection()
                    .stream()
                    .map(ts -> ts.getData())
                    .filter(s->!s.isEmpty())
                    .reduce(TsData::add)
                    .map(s-> s == null || s.isEmpty() ? null : Ts.builder().moniker(TsMoniker.of()).data(s).name("Sum").build());
            aggChart.setTsCollection(sum.map(TsCollection::of).orElse(TsCollection.EMPTY));
        });
    }
}
