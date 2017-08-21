package de.bioforscher.singa.chemistry.parser.pdb.structures;

/**
 * Options for faster or more detailed parsing.
 *
 * @author cl
 */
public class StructureParserOptions {

    /**
     * Settings that can be passed to the Options.
     */
    public enum Setting {

        /**
         * Create Edges in between leaf substructures (default).
         */
        CREATE_EDGES,

        /**
         * Omit Edges between leaf substructures.
         */
        OMIT_EDGES,

        /**
         * Parse additional information for ligands form pdb (default).
         */
        GET_LIGAND_INFORMATION,

        /**
         * Omit parsing of ligand information.
         */
        OMIT_LIGAND_INFORMATION,

        /**
         * Connect hydrogens to leafs.
         */
        GET_HYDROGEN_CONNECTIONS,

        /**
         * Omit hydrogen connections to leafs (default).
         */
        OMIT_HYDROGENS_CONNECTIONS,

        /**
         * Parse hydrogen atoms.
         */
        GET_HYDROGENS,

        /**
         * Omit hydrogen atoms (default).
         */
        OMIT_HYDROGENS,

        /**
         * Use the file name as the title of the structure.
         */
        GET_TITLE_FROM_FILENAME,

        /**
         * Use the pdb file to infer the pdb title (default).
         */
        GET_TITLE_FROM_PDB,

        /**
         * Parse and use the first valid pdb identifier from the file name.
         */
        GET_IDENTIFIER_FROM_FILENAME,

        /**
         * Use the pdb file to infer the pdb identifier (default).
         */
        GET_IDENTIFIER_FROM_PDB

    }

    /**
     * Create a new Options object using enum constants.
     * @param settings The settings.
     * @return The options.
     */
    public static StructureParserOptions withSettings(Setting ... settings) {
        StructureParserOptions options = new StructureParserOptions();
        for (Setting setting: settings) {
            setOption(options, setting);
        }
        return options;
    }

    /**
     * Sets the any option.
     * @param options The options object to set.
     * @param setting The settings.
     */
    private static void setOption(StructureParserOptions options, Setting setting) {
        switch (setting) {
            case CREATE_EDGES:
                options.createEdges(true);
                break;
            case OMIT_EDGES:
                options.createEdges(false);
                break;
            case GET_LIGAND_INFORMATION:
                options.retrieveLigandInformation(true);
                break;
            case OMIT_LIGAND_INFORMATION:
                options.retrieveLigandInformation(false);
                break;
            case GET_HYDROGEN_CONNECTIONS:
                options.connectHydrogens(true);
                break;
            case OMIT_HYDROGENS_CONNECTIONS:
                options.connectHydrogens(false);
                break;
            case GET_HYDROGENS:
                options.omitHydrogens(false);
                break;
            case OMIT_HYDROGENS:
                options.omitHydrogens(true);
                break;
            case GET_TITLE_FROM_FILENAME:
                options.inferTitleFromFileName(true);
                break;
            case GET_TITLE_FROM_PDB:
                options.inferTitleFromFileName(false);
                break;
            case GET_IDENTIFIER_FROM_FILENAME:
                options.inferIdentifierFromFileName(true);
                break;
            case GET_IDENTIFIER_FROM_PDB:
                options.inferIdentifierFromFileName(false);
                break;
        }
    }

    /**
     * Creates edges in the graph.
     */
    private boolean createEdges = true;

    /**
     * Retrieves additional information from CIF files.
     */
    private boolean retrieveLigandInformation = true;

    /**
     * Tries to saturate the residue with hydrogen atoms if they are in the given map of atoms.
     */
    private boolean connectHydrogens = false;

    /**
     * Omits all hydrogen (and eventually deuterium) atoms. Those atoms are not added to the resulting residue.
     */
    private boolean omitHydrogens = false;

    /**
     * Uses the filename as the title of the parsed structure.
     */
    private boolean inferTitleFromFileName = false;

    /**
     * Tries to find a pdbid in the file name and uses it as the identifier of the structure.
     */
    private boolean inferIdentifierFromFileName = false;

    /**
     * Setting this option true will connect the molecule with bonds. Turning the option {@code false} will result in
     * faster parsing, if those information are not strictly required.<br><br>
     * Default value: {@code true}
     *
     * @return The value of this option.
     */
    public boolean isCreatingEdges() {
        return this.createEdges;
    }

    /**
     * Setting this option true will connect the molecule with bonds. Turning the option {@code false} will result in
     * faster parsing, if those information are not strictly required.<br><br>
     * Default value: {@code true}
     *
     * @param createEdges {@code true} to turn this option on, {@code false} to turn it off.
     */
    public void createEdges(boolean createEdges) {
        this.createEdges = createEdges;
    }

    /**
     * Setting this option to {@code true} will retrieve additional information from cif files from the PDB. This
     * enables the detection modified amino acids and nucleotides als well as the retrieval of additional information
     * for ligands and the connection of ligand atoms. Turning the option {@code false} will result in faster parsing,
     * if those information are not strictly required.<br><br>
     * Default value: {@code true}
     *
     * @return The value of this option.
     */
    public boolean isRetrievingLigandInformation() {
        return this.retrieveLigandInformation;
    }

    /**
     * Setting this option to {@code true} will retrieve additional information from cif files from the PDB. This
     * enables the detection modified amino acids and nucleotides als well as the retrieval of additional information
     * for ligands and the connection of ligand atoms. Turning the option {@code false} will result in faster parsing,
     * if those information are not strictly required.<br><br>
     * Default value: {@code true}
     *
     * @param retrieveLigandInformation {@code true} to turn this option on, {@code false} to turn it off.
     */
    public void retrieveLigandInformation(boolean retrieveLigandInformation) {
        this.retrieveLigandInformation = retrieveLigandInformation;
    }

    /**
     * Setting this option to {@code true} will try to connect hydrogen atom tho their respective partners.<br><br>
     * Default value: {@code false}
     *
     * @return The value of this option.
     */
    public boolean isConnectingHydrogens() {
        return this.connectHydrogens;
    }

    /**
     * Setting this option to {@code true} will try to connect hydrogen atom tho their respective partners.<br><br>
     * Default value: {@code false}
     *
     * @param connectHydrogens {@code true} to turn this option on, {@code false} to turn it off.
     */
    public void connectHydrogens(boolean connectHydrogens) {
        this.connectHydrogens = connectHydrogens;
    }

    /**
     * Setting this option to {@code true} will omit all hydrogen (and eventually deuterium) atoms in amino acids and
     * nucleotides.
     * Default value: {@code false}
     *
     * @return {@code true} to turn this option on, {@code false} to turn it off.
     */
    public boolean isOmittingHydrogen() {
        return this.omitHydrogens;
    }

    /**
     * Setting this option to {@code true} will omit all hydrogen (and eventually deuterium) atoms in amino acids and
     * nucleotides.
     * Default value: {@code false}
     *
     * @param omitHydrogens {@code true} to turn this option on, {@code false} to turn it off.
     */
    public void omitHydrogens(boolean omitHydrogens) {
        this.omitHydrogens = omitHydrogens;
        if (omitHydrogens) {
            this.connectHydrogens = false;
        }
    }

    /**
     * Setting this option to {@code true} will set the title of the structure to the file name.
     * Default value: {@code false}
     *
     * @return {@code true} to turn this option on, {@code false} to turn it off.
     */
    public boolean isInferringTitleFromFileName() {
        return this.inferTitleFromFileName;
    }

    /**
     * Setting this option to {@code true} will set the title of the structure to the file name.
     * Default value: {@code false}
     *
     * @param inferTitleFromFileName {@code true} to turn this option on, {@code false} to turn it off.
     */
    public void inferTitleFromFileName(boolean inferTitleFromFileName) {
        this.inferTitleFromFileName = inferTitleFromFileName;
    }

    /**
     * Setting this option to {@code true} will try parse any pdb identifier from the file name.
     * Default value: {@code false}
     *
     * @return {@code true} to turn this option on, {@code false} to turn it off.
     */
    public boolean isInferringIdentifierFromFileName() {
        return this.inferIdentifierFromFileName;
    }

    /**
     * Setting this option to {@code true} will try parse any pdb identifier from the file name.
     * Default value: {@code false}
     *
     * @param inferIdentifierFromFileName {@code true} to turn this option on, {@code false} to turn it off.
     */
    public void inferIdentifierFromFileName(boolean inferIdentifierFromFileName) {
        this.inferIdentifierFromFileName = inferIdentifierFromFileName;
    }
}
