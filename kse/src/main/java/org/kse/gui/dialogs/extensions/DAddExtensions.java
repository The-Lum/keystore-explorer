/*
 * Copyright 2004 - 2013 Wayne Grant
 *           2013 - 2025 Kai Kramer
 *
 * This file is part of KeyStore Explorer.
 *
 * KeyStore Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyStore Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyStore Explorer.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kse.gui.dialogs.extensions;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.kse.KSE;
import org.kse.crypto.keypair.KeyPairType;
import org.kse.crypto.keypair.KeyPairUtil;
import org.kse.crypto.publickey.KeyIdentifierGenerator;
import org.kse.crypto.x509.ExtendedKeyUsageType;
import org.kse.crypto.x509.GeneralNameUtil;
import org.kse.crypto.x509.X509Ext;
import org.kse.crypto.x509.X509ExtensionSet;
import org.kse.crypto.x509.X509ExtensionSetLoadException;
import org.kse.crypto.x509.X509ExtensionSetUpdater;
import org.kse.crypto.x509.X509ExtensionType;
import org.kse.gui.CurrentDirectory;
import org.kse.gui.CursorUtil;
import org.kse.gui.FileChooserFactory;
import org.kse.gui.JKseTable;
import org.kse.gui.PlatformUtil;
import org.kse.gui.components.JEscDialog;
import org.kse.gui.error.DError;
import org.kse.utilities.DialogViewer;
import org.kse.utilities.oid.ObjectIdComparator;
import org.kse.utilities.os.OperatingSystem;

import net.miginfocom.swing.MigLayout;

/**
 * Allows selection of X.509 Extensions to add to a certificate.
 */
public class DAddExtensions extends JEscDialog {
    private static final long serialVersionUID = 1L;

    private static ResourceBundle res = ResourceBundle.getBundle("org/kse/gui/dialogs/extensions/resources");

    private static final String CANCEL_KEY = "CANCEL_KEY";

    private JButton jbAdd;
    private JButton jbEdit;
    private JButton jbToggleCriticality;
    private JButton jbRemove;
    private JScrollPane jspExtensionsTable;
    private JKseTable jtExtensions;
    private JButton jbSelectStandardTemplate;
    private JButton jbLoadTemplate;
    private JButton jbSaveTemplate;
    private JPanel jpButtons;
    private JButton jbOK;
    private JButton jbCancel;

    private X509ExtensionSet extensions = new X509ExtensionSet();
    private PublicKey issuerPublicKey;
    private X500Name issuerCertName;
    private BigInteger issuerCertSerialNumber;
    private byte[] issuerSki;
    private PublicKey subjectPublicKey;
    private X500Name subjectCertName;

    /**
     * Creates a new DAddExtensions dialog.
     *
     * @param parent                 Parent frame
     * @param title                  The dialog title
     * @param extensions             Extensions to add to
     * @param issuerPublicKey        Authority public key
     * @param issuerCertName         Authority certificate name
     * @param issuerCertSerialNumber Authority certificate serial number
     * @param issuerSki              Authority certificate Subject Key Identifier
     * @param subjectPublicKey       Subject public key
     * @param subjectCertName        Subject DN
     */
    public DAddExtensions(JFrame parent, String title, X509ExtensionSet extensions, PublicKey issuerPublicKey,
                          X500Name issuerCertName, BigInteger issuerCertSerialNumber, byte[] issuerSki,
                          PublicKey subjectPublicKey, X500Name subjectCertName) {

        super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
        setTitle(res.getString("DAddExtensions.Title"));
        this.extensions = extensions;
        this.issuerPublicKey = issuerPublicKey;
        this.issuerCertName = issuerCertName;
        this.issuerCertSerialNumber = issuerCertSerialNumber;
        this.issuerSki = issuerSki;
        this.subjectPublicKey = subjectPublicKey;
        this.subjectCertName = subjectCertName;
        initComponents();
    }

    /**
     * Creates new DAddExtensions dialog.
     *
     * @param parent                 Parent dialog
     * @param extensions             Extensions to add to
     * @param issuerPublicKey        Authority public key
     * @param issuerCertName         Authority certificate name
     * @param issuerCertSerialNumber Authority certificate serial number
     * @param issuerSki              Authority certificate Subject Key Identifier
     * @param subjectPublicKey       Subject public key
     * @param subjectCertName        Subject DN
     */
    public DAddExtensions(JDialog parent, X509ExtensionSet extensions, PublicKey issuerPublicKey,
                          X500Name issuerCertName, BigInteger issuerCertSerialNumber, byte[] issuerSki,
                          PublicKey subjectPublicKey, X500Name subjectCertName) {

        super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
        setTitle(res.getString("DAddExtensions.Title"));
        this.extensions = (X509ExtensionSet) extensions.clone();
        this.issuerPublicKey = issuerPublicKey;
        this.issuerCertName = issuerCertName;
        this.issuerCertSerialNumber = issuerCertSerialNumber;
        this.issuerSki = issuerSki;
        this.subjectPublicKey = subjectPublicKey;
        this.subjectCertName = subjectCertName;
        initComponents();
    }

    private void initComponents() {
        jbAdd = new JButton(
                new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/add_ext.png"))));
        jbAdd.setMargin(new Insets(2, 2, 2, 2));
        jbAdd.setToolTipText(res.getString("DAddExtensions.jbAdd.tooltip"));
        jbAdd.setMnemonic(res.getString("DAddExtensions.jbAdd.mnemonic").charAt(0));

        jbAdd.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                addPressed();
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        jbEdit = new JButton(
                new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/edit_ext.png"))));
        jbEdit.setMargin(new Insets(2, 2, 2, 2));
        jbEdit.setToolTipText(res.getString("DAddExtensions.jbEdit.tooltip"));
        jbEdit.setMnemonic(res.getString("DAddExtensions.jbEdit.mnemonic").charAt(0));

        jbEdit.setEnabled(false);

        jbEdit.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                editPressed();
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        jbToggleCriticality = new JButton(new ImageIcon(
                Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/toggle_ext_crit.png"))));
        jbToggleCriticality.setMargin(new Insets(2, 2, 2, 2));
        jbToggleCriticality.setToolTipText(res.getString("DAddExtensions.jbToggleCriticality.tooltip"));
        jbToggleCriticality.setMnemonic(res.getString("DAddExtensions.jbToggleCriticality.mnemonic").charAt(0));

        jbToggleCriticality.setEnabled(false);

        jbToggleCriticality.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                toggleCriticalityPressed();
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        jbRemove = new JButton(new ImageIcon(
                Toolkit.getDefaultToolkit().createImage(getClass().getResource("images/remove_ext.png"))));
        jbRemove.setMargin(new Insets(2, 2, 2, 2));
        jbRemove.setToolTipText(res.getString("DAddExtensions.jbRemove.tooltip"));
        jbRemove.setMnemonic(res.getString("DAddExtensions.jbRemove.mnemonic").charAt(0));

        jbRemove.setEnabled(false);

        jbRemove.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                removePressed();
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        ExtensionsTableModel extensionsTableModel = new ExtensionsTableModel();
        jtExtensions = new JKseTable(extensionsTableModel);

        TableRowSorter<ExtensionsTableModel> sorter = new TableRowSorter<>(extensionsTableModel);
        sorter.setComparator(2, new ObjectIdComparator());
        jtExtensions.setRowSorter(sorter);

        jtExtensions.setShowGrid(false);
        jtExtensions.setRowMargin(0);
        jtExtensions.getColumnModel().setColumnMargin(0);
        jtExtensions.getTableHeader().setReorderingAllowed(false);
        jtExtensions.setAutoResizeMode(JKseTable.AUTO_RESIZE_ALL_COLUMNS);
        jtExtensions.setRowHeight(Math.max(18, jtExtensions.getRowHeight()));

        for (int i = 0; i < jtExtensions.getColumnCount(); i++) {
            TableColumn column = jtExtensions.getColumnModel().getColumn(i);
            column.setHeaderRenderer(new ExtensionsTableHeadRend(jtExtensions.getTableHeader().getDefaultRenderer()));
            column.setCellRenderer(new ExtensionsTableCellRend());
        }

        TableColumn criticalCol = jtExtensions.getColumnModel().getColumn(0);
        criticalCol.setResizable(false);
        criticalCol.setMinWidth(28);
        criticalCol.setMaxWidth(28);
        criticalCol.setPreferredWidth(28);

        ListSelectionModel selectionModel = jtExtensions.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                try {
                    CursorUtil.setCursorBusy(DAddExtensions.this);
                    updateButtonControls();
                } finally {
                    CursorUtil.setCursorFree(DAddExtensions.this);
                }

            }
        });

        jtExtensions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                maybeEditExtension(evt);
            }
        });

        jtExtensions.addKeyListener(new KeyAdapter() {
            boolean deleteLastPressed = false;

            @Override
            public void keyPressed(KeyEvent evt) {
                // Record delete pressed on non-Macs
                if (!OperatingSystem.isMacOs()) {
                    deleteLastPressed = evt.getKeyCode() == KeyEvent.VK_DELETE;
                }
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                // Delete on non-Mac if delete was pressed and is now released
                if (!OperatingSystem.isMacOs() && deleteLastPressed && evt.getKeyCode() == KeyEvent.VK_DELETE) {
                    try {
                        CursorUtil.setCursorBusy(DAddExtensions.this);
                        deleteLastPressed = false;
                        removeSelectedExtension();
                    } finally {
                        CursorUtil.setCursorFree(DAddExtensions.this);
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent evt) {
                // Delete on Mac if backspace typed
                if (OperatingSystem.isMacOs() && evt.getKeyChar() == 0x08) {
                    try {
                        CursorUtil.setCursorBusy(DAddExtensions.this);
                        removeSelectedExtension();
                    } finally {
                        CursorUtil.setCursorFree(DAddExtensions.this);
                    }
                }
            }
        });

        jspExtensionsTable = PlatformUtil.createScrollPane(jtExtensions,
                                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspExtensionsTable.getViewport().setBackground(jtExtensions.getBackground());
        jspExtensionsTable.setPreferredSize(new Dimension(400, 250));

        jbSelectStandardTemplate = new JButton(res.getString("DAddExtensions.jbSelectStandardTemplate.text"));
        jbSelectStandardTemplate.setMnemonic(
                res.getString("DAddExtensions.jbSelectStandardTemplate.mnemonic").charAt(0));
        jbSelectStandardTemplate.setToolTipText(res.getString("DAddExtensions.jbSelectStandardTemplate.tooltip"));

        jbSelectStandardTemplate.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                selectStandardTemplatePressed();
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        jbLoadTemplate = new JButton(res.getString("DAddExtensions.jbLoadTemplate.text"));
        jbLoadTemplate.setMnemonic(res.getString("DAddExtensions.jbLoadTemplate.mnemonic").charAt(0));
        jbLoadTemplate.setToolTipText(res.getString("DAddExtensions.jbLoadTemplate.tooltip"));

        jbLoadTemplate.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                loadTemplatePressed();
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        jbSaveTemplate = new JButton(res.getString("DAddExtensions.jbSaveTemplate.text"));
        jbSaveTemplate.setMnemonic(res.getString("DAddExtensions.jbSaveTemplate.mnemonic").charAt(0));
        jbSaveTemplate.setToolTipText(res.getString("DAddExtensions.jbSaveTemplate.tooltip"));

        jbSaveTemplate.addActionListener(evt -> {
            try {
                CursorUtil.setCursorBusy(DAddExtensions.this);
                saveTemplatePressed(extensions, DAddExtensions.this);
            } finally {
                CursorUtil.setCursorFree(DAddExtensions.this);
            }
        });

        jbOK = new JButton(res.getString("DAddExtensions.jbOK.text"));
        jbOK.addActionListener(evt -> okPressed());

        jbCancel = new JButton(res.getString("DAddExtensions.jbCancel.text"));
        jbCancel.addActionListener(evt -> cancelPressed());
        jbCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_KEY);
        jbCancel.getActionMap().put(CANCEL_KEY, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                cancelPressed();
            }
        });

        jpButtons = PlatformUtil.createDialogButtonPanel(jbOK, jbCancel, "insets 0");

        reloadExtensionsTable();
        selectFirstExtensionInTable();
        updateButtonControls();

        // layout
        Container pane = getContentPane();
        pane.setLayout(new MigLayout("insets dialog, fill", "[]", "[]"));
        pane.add(jspExtensionsTable, "growx, pushx");
        pane.add(jbAdd, "split 4, flowy");
        pane.add(jbEdit);
        pane.add(jbToggleCriticality);
        pane.add(jbRemove, "wrap rel");
        pane.add(jbSelectStandardTemplate, "right, spanx, split 3");
        pane.add(jbLoadTemplate);
        pane.add(jbSaveTemplate);
        pane.add(new JSeparator(), "spanx, growx");
        pane.add(jpButtons, "right, growx, spanx");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(jbOK);

        pack();
    }

    private void addPressed() {
        DAddExtensionType dAddExtensionType = new DAddExtensionType(this, extensions);
        dAddExtensionType.setLocationRelativeTo(this);
        dAddExtensionType.setVisible(true);

        X509ExtensionType extensionTypeToAdd = dAddExtensionType.getExtensionType();

        if (extensionTypeToAdd == null) {
            return;
        }

        boolean isCritical = dAddExtensionType.isExtensionCritical();
        DExtension dExtension = determineExtensionDialog(extensionTypeToAdd);
        if (dExtension == null) {
            return;
        }
        dExtension.setLocationRelativeTo(this);
        dExtension.setVisible(true);

        byte[] extensionValue = dExtension.getValue();
        String oid = dExtension.getOid();

        if (extensionValue == null || oid == null) {
            return;
        }

        // value has to be wrapped in a DER-encoded OCTET STRING
        byte[] extensionValueOctet = null;
        try {
            extensionValueOctet = new DEROctetString(extensionValue).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            return;
        }

        extensions.addExtension(oid, isCritical, extensionValueOctet);

        reloadExtensionsTable();
        selectExtensionInTable(oid);
        updateButtonControls();
    }

    private DExtension determineExtensionDialog(X509ExtensionType extensionType) {
        DExtension dExtension = null;

        switch (extensionType) {
        case AUTHORITY_INFORMATION_ACCESS:
            dExtension = new DAuthorityInformationAccess(this);
            break;
        case AUTHORITY_KEY_IDENTIFIER:
            dExtension = new DAuthorityKeyIdentifier(this, issuerPublicKey, issuerCertName, issuerCertSerialNumber,
                    issuerSki);
            break;
        case BASIC_CONSTRAINTS:
            dExtension = new DBasicConstraints(this);
            break;
        case CERTIFICATE_POLICIES:
            dExtension = new DCertificatePolicies(this);
            break;
        case EXTENDED_KEY_USAGE:
            dExtension = new DExtendedKeyUsage(this);
            break;
        case INHIBIT_ANY_POLICY:
            dExtension = new DInhibitAnyPolicy(this);
            break;
        case ISSUER_ALTERNATIVE_NAME:
            dExtension = new DIssuerAlternativeName(this);
            break;
        case KEY_USAGE:
            dExtension = new DKeyUsage(this);
            break;
        case NAME_CONSTRAINTS:
            dExtension = new DNameConstraints(this);
            break;
        case POLICY_CONSTRAINTS:
            dExtension = new DPolicyConstraints(this);
            break;
        case POLICY_MAPPINGS:
            dExtension = new DPolicyMappings(this);
            break;
        case PRIVATE_KEY_USAGE_PERIOD:
            dExtension = new DPrivateKeyUsagePeriod(this);
            break;
        case SUBJECT_ALTERNATIVE_NAME:
            dExtension = new DSubjectAlternativeName(this);
            break;
        case SUBJECT_INFORMATION_ACCESS:
            dExtension = new DSubjectInformationAccess(this);
            break;
        case SUBJECT_KEY_IDENTIFIER:
            dExtension = new DSubjectKeyIdentifier(this, subjectPublicKey);
            break;
        case CRL_DISTRIBUTION_POINTS:
            dExtension = new DCrlDistributionPoints(this);
            break;
        case CUSTOM:
            dExtension = new DCustomExtension(this);
            break;
        default:
            return null;
        }

        return dExtension;
    }

    private void editPressed() {
        editSelectedExtension();
    }

    private void maybeEditExtension(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            Point point = new Point(evt.getX(), evt.getY());
            int row = jtExtensions.rowAtPoint(point);

            if (row != -1) {
                try {
                    CursorUtil.setCursorBusy(DAddExtensions.this);
                    jtExtensions.setRowSelectionInterval(row, row);
                    editSelectedExtension();
                } finally {
                    CursorUtil.setCursorFree(DAddExtensions.this);
                }
            }
        }
    }

    private void editSelectedExtension() {
        try {
            int selectedRow = jtExtensions.getSelectedRow();

            if (selectedRow != -1) {
                String oid = ((ASN1ObjectIdentifier) jtExtensions.getValueAt(selectedRow, 2)).getId();
                X509ExtensionType extensionType = X509ExtensionType.resolveOid(oid);

                byte[] extensionValue = ASN1OctetString.getInstance(extensions.getExtensionValue(oid)).getOctets();
                boolean isCritical = extensions.getCriticalExtensionOIDs().contains(oid);

                DExtension dExtension = null;

                switch (extensionType) {
                case AUTHORITY_INFORMATION_ACCESS:
                    dExtension = new DAuthorityInformationAccess(this, extensionValue);
                    break;
                case AUTHORITY_KEY_IDENTIFIER:
                    dExtension = new DAuthorityKeyIdentifier(this, extensionValue, issuerPublicKey, issuerSki);
                    break;
                case BASIC_CONSTRAINTS:
                    dExtension = new DBasicConstraints(this, extensionValue);
                    break;
                case CERTIFICATE_POLICIES:
                    dExtension = new DCertificatePolicies(this, extensionValue);
                    break;
                case EXTENDED_KEY_USAGE:
                    dExtension = new DExtendedKeyUsage(this, extensionValue);
                    break;
                case INHIBIT_ANY_POLICY:
                    dExtension = new DInhibitAnyPolicy(this, extensionValue);
                    break;
                case ISSUER_ALTERNATIVE_NAME:
                    dExtension = new DIssuerAlternativeName(this, extensionValue);
                    break;
                case KEY_USAGE:
                    dExtension = new DKeyUsage(this, extensionValue);
                    break;
                case NAME_CONSTRAINTS:
                    dExtension = new DNameConstraints(this, extensionValue);
                    break;
                case POLICY_CONSTRAINTS:
                    dExtension = new DPolicyConstraints(this, extensionValue);
                    break;
                case POLICY_MAPPINGS:
                    dExtension = new DPolicyMappings(this, extensionValue);
                    break;
                case PRIVATE_KEY_USAGE_PERIOD:
                    dExtension = new DPrivateKeyUsagePeriod(this, extensionValue);
                    break;
                case SUBJECT_ALTERNATIVE_NAME:
                    dExtension = new DSubjectAlternativeName(this, extensionValue);
                    break;
                case SUBJECT_INFORMATION_ACCESS:
                    dExtension = new DSubjectInformationAccess(this, extensionValue);
                    break;
                case SUBJECT_KEY_IDENTIFIER:
                    dExtension = new DSubjectKeyIdentifier(this, extensionValue, subjectPublicKey);
                    break;
                case CRL_DISTRIBUTION_POINTS:
                    dExtension = new DCrlDistributionPoints(this, extensionValue);
                    break;
                case UNKNOWN:
                    dExtension = new DCustomExtension(this, oid, extensionValue);
                    break;
                default:
                    return;
                }

                dExtension.setLocationRelativeTo(this);
                dExtension.setVisible(true);
                byte[] newExtensionValue = dExtension.getValue();
                String newOid = dExtension.getOid();

                if (newExtensionValue == null || newOid == null) {
                    return;
                }

                // value has to be wrapped in a DER-encoded OCTET STRING
                byte[] newExtensionValueOctet;
                if (newExtensionValue.length == 0) {
                    // empty extension value is possible, e.g. for id-pkix-ocsp-nocheck from RFC 6960
                    newExtensionValueOctet = DERNull.INSTANCE.getEncoded(ASN1Encoding.DER);
                } else {
                    newExtensionValueOctet = new DEROctetString(newExtensionValue).getEncoded(ASN1Encoding.DER);
                }

                extensions.removeExtension(oid);
                extensions.addExtension(newOid, isCritical, newExtensionValueOctet);

                reloadExtensionsTable();
                selectExtensionInTable(newOid);
                updateButtonControls();
            }
        } catch (Exception e) {
            DError.displayError(this, e);
        }
    }

    private void toggleCriticalityPressed() {
        int selectedRow = jtExtensions.getSelectedRow();

        if (selectedRow != -1) {
            String oid = ((ASN1ObjectIdentifier) jtExtensions.getValueAt(selectedRow, 2)).getId();

            extensions.toggleExtensionCriticality(oid);

            reloadExtensionsTable();
            selectFirstExtensionInTable();
            updateButtonControls();
        }
    }

    private void removePressed() {
        removeSelectedExtension();
    }

    private void removeSelectedExtension() {
        int selectedRow = jtExtensions.getSelectedRow();

        if (selectedRow != -1) {
            String oid = ((ASN1ObjectIdentifier) jtExtensions.getValueAt(selectedRow, 2)).getId();

            extensions.removeExtension(oid);

            reloadExtensionsTable();
            selectFirstExtensionInTable();
            updateButtonControls();
        }
    }

    private ExtensionsTableModel getExtensionsTableModel() {
        return (ExtensionsTableModel) jtExtensions.getModel();
    }

    private void selectFirstExtensionInTable() {
        if (getExtensionsTableModel().getRowCount() > 0) {
            jtExtensions.changeSelection(0, 0, false, false);
        }
    }

    private void selectExtensionInTable(String oid) {
        for (int i = 0; i < jtExtensions.getRowCount(); i++) {
            if (oid.equals(((ASN1ObjectIdentifier) jtExtensions.getValueAt(i, 2)).getId())) {
                jtExtensions.changeSelection(i, 0, false, false);
                return;
            }
        }
    }

    private void reloadExtensionsTable() {
        getExtensionsTableModel().load(extensions);
    }

    private void updateButtonControls() {
        int selectedRow = jtExtensions.getSelectedRow();

        if (selectedRow == -1) {
            jbEdit.setEnabled(false);
            jbToggleCriticality.setEnabled(false);
            jbRemove.setEnabled(false);
        } else {
            jbEdit.setEnabled(true);
            jbToggleCriticality.setEnabled(true);
            jbRemove.setEnabled(true);
        }
    }

    private void selectStandardTemplatePressed() {
        DSelectStandardExtensionTemplate dSelectStdCertTemplate = new DSelectStandardExtensionTemplate(this,
                                                                                                       issuerPublicKey,
                                                                                                       subjectPublicKey,
                                                                                                       subjectCertName,
                                                                                                       issuerSki);
        dSelectStdCertTemplate.setLocationRelativeTo(this);
        dSelectStdCertTemplate.setVisible(true);

        if (!dSelectStdCertTemplate.wasCancelled()) {
            this.extensions = dSelectStdCertTemplate.getExtensionSet();
            reloadExtensionsTable();
            selectFirstExtensionInTable();
        }
    }

    private void loadTemplatePressed() {
        JFileChooser chooser = FileChooserFactory.getCetFileChooser();

        chooser.setCurrentDirectory(CurrentDirectory.get());
        chooser.setDialogTitle(res.getString("DAddExtensions.LoadCet.Title"));
        chooser.setMultiSelectionEnabled(false);

        chooser.setApproveButtonText(res.getString("DAddExtensions.CetLoad.button"));

        int rtnValue = chooser.showOpenDialog(this);
        if (rtnValue == JFileChooser.APPROVE_OPTION) {
            File loadFile = chooser.getSelectedFile();
            CurrentDirectory.updateForFile(loadFile);

            try {
                extensions = X509ExtensionSet.load(new FileInputStream(loadFile));

                X509ExtensionSetUpdater.update(extensions, subjectPublicKey, issuerPublicKey, issuerCertName,
                                               issuerCertSerialNumber);

                reloadExtensionsTable();
                selectFirstExtensionInTable();
                updateButtonControls();
            } catch (X509ExtensionSetLoadException ex) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                                                      res.getString("DAddExtensions.InvalidCetFile.message"), loadFile),
                                              res.getString("DAddExtensions.LoadCet.Title"),
                                              JOptionPane.WARNING_MESSAGE);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this,
                                              MessageFormat.format(res.getString("DAddExtensions.NoReadFile.message"),
                                                                   loadFile),
                                              res.getString("DAddExtensions.LoadCet.Title"),
                                              JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                DError.displayError(this, ex);
            }
        }
    }

    /**
     * Prompt the user to save the given set of extensions into a template file, via a save dialog which will be the
     * child of the given JDialog.
     *
     * @param extensionsToSave The extensions to save into the template file.
     * @param parentComponent  The parent JDialog of the newly created save dialog window.
     */
    public static void saveTemplatePressed(X509ExtensionSet extensionsToSave, JDialog parentComponent) {
        JFileChooser chooser = FileChooserFactory.getCetFileChooser();

        chooser.setCurrentDirectory(CurrentDirectory.get());
        chooser.setDialogTitle(res.getString("DAddExtensions.SaveCet.Title"));
        chooser.setMultiSelectionEnabled(false);

        int rtnValue = chooser.showSaveDialog(parentComponent);
        if (rtnValue == JFileChooser.APPROVE_OPTION) {
            File saveFile = chooser.getSelectedFile();
            CurrentDirectory.updateForFile(saveFile);

            if (saveFile.isFile()) {
                String message = MessageFormat.format(res.getString("DAddExtensions.OverWriteFile.message"), saveFile);

                int selected = JOptionPane.showConfirmDialog(parentComponent, message,
                                                             res.getString("DAddExtensions.SaveCet.Title"),
                                                             JOptionPane.YES_NO_OPTION);
                if (selected != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                extensionsToSave.save(new FileOutputStream(saveFile));
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(parentComponent,
                                              MessageFormat.format(res.getString("DAddExtensions.NoWriteFile.message"),
                                                                   saveFile),
                                              res.getString("DAddExtensions.SaveCet.Title"),
                                              JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                DError.displayError(parentComponent, ex);
            }
        }
    }

    /**
     * Get chosen certificate extensions.
     *
     * @return Certificate extensions or null if dialog cancelled.
     */
    public X509ExtensionSet getExtensions() {
        return extensions;
    }

    private boolean isSanExtensionEmpty() {
        byte[] extensionValue = extensions.getExtensionValue(X509ExtensionType.SUBJECT_ALTERNATIVE_NAME.oid());
        if (extensionValue == null) {
            return false;
        }

        byte[] unwrappedExtension = X509Ext.unwrapExtension(extensionValue);
        GeneralNames generalNames = GeneralNames.getInstance(unwrappedExtension);
        GeneralName[] names = generalNames.getNames();
        if (names == null || names.length == 0) {
            return true;
        }
        for (GeneralName generalName : names) {
            if (GeneralNameUtil.isGeneralNameEmpty(generalName)) {
                return true;
            }
        }
        return false;
    }

    private void okPressed() {
        if (isSanExtensionEmpty()) {
            JOptionPane.showMessageDialog(this, res.getString("DAddExtensions.EmptySAN.message"),
                                          res.getString("DAddExtensions.EmptySAN.Title"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        closeDialog();
    }

    private void cancelPressed() {
        extensions = null;
        closeDialog();
    }

    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    // for quick UI testing
    public static void main(String[] args) throws Exception {
        final KeyPair keyPair = KeyPairUtil.generateKeyPair(KeyPairType.RSA, 1024, KSE.BC);

        X509ExtensionSet extensionSet = new X509ExtensionSet();

        KeyIdentifierGenerator akiGenerator = new KeyIdentifierGenerator(keyPair.getPublic());
        AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(akiGenerator.generate160BitHashId());
        byte[] akiEncoded = X509Ext.wrapInOctetString(aki.getEncoded());
        extensionSet.addExtension(X509ExtensionType.AUTHORITY_KEY_IDENTIFIER.oid(), false, akiEncoded);

        KeyIdentifierGenerator skiGenerator = new KeyIdentifierGenerator(keyPair.getPublic());
        SubjectKeyIdentifier ski = new SubjectKeyIdentifier(skiGenerator.generate160BitHashId());
        byte[] skiEncoded = X509Ext.wrapInOctetString(ski.getEncoded());
        extensionSet.addExtension(X509ExtensionType.SUBJECT_KEY_IDENTIFIER.oid(), false, skiEncoded);

        BasicConstraints bc = new BasicConstraints(true);
        byte[] bcEncoded = X509Ext.wrapInOctetString(bc.getEncoded());
        extensionSet.addExtension(X509ExtensionType.BASIC_CONSTRAINTS.oid(), true, bcEncoded);

        KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
        byte[] kuEncoded = X509Ext.wrapInOctetString(ku.getEncoded());
        extensionSet.addExtension(X509ExtensionType.KEY_USAGE.oid(), true, kuEncoded);

        ExtendedKeyUsage eku = new ExtendedKeyUsage(new KeyPurposeId[] {
                KeyPurposeId.getInstance(new ASN1ObjectIdentifier(ExtendedKeyUsageType.SERVER_AUTH.oid())) });
        byte[] ekuEncoded = X509Ext.wrapInOctetString(eku.getEncoded());
        extensionSet.addExtension(X509ExtensionType.EXTENDED_KEY_USAGE.oid(), false, ekuEncoded);

        DAddExtensions dialog = new DAddExtensions(new JFrame(), "Add Extensions", extensionSet,
                                                   keyPair.getPublic(), new X500Name("cn=test"), BigInteger.ONE,
                                                   null, keyPair.getPublic(), new X500Name("cn=www.example.com"));
        DialogViewer.run(dialog);
    }
}
