package quilt.internal.tasks.build;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import quilt.internal.Constants;
import quilt.internal.tasks.setup.CheckIntermediaryMappingsTask;
import quilt.internal.tasks.setup.DownloadIntermediaryMappingsTask;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MergeIntermediaryTask extends AbstractTinyMergeTask {
    public static final String TASK_NAME = "mergeIntermediary";

    public MergeIntermediaryTask() {
        super("mappings-intermediaryMerged.tiny", "intermediary", Constants.PER_VERSION_MAPPINGS_NAME);
        dependsOn(CheckIntermediaryMappingsTask.TASK_NAME, DownloadIntermediaryMappingsTask.TASK_NAME, MergeTinyV2Task.TASK_NAME);
        onlyIf(task -> getTaskByType(CheckIntermediaryMappingsTask.class).isPresent());

        input.convention(getTaskByType(DownloadIntermediaryMappingsTask.class)::getTinyFile);
    }

    @Override
    public void mergeMappings() throws Exception {
        File tinyInput = this.getTaskByType(MergeTinyV2Task.class).getOutputMappings();
        mergeMappings(tinyInput);
    }

    @Override
    protected MappingVisitor getFirstVisitor(MappingVisitor next) {
        // Fill missing hashed names with their corresponding unobfuscated official name
        // Ran first for unnamed classes to also be filled with those names
        return new MappingNsCompleter(next, Collections.singletonMap(this.fillName, "official"));
    }

    @Override
    protected MappingVisitor getPreWriteVisitor(MappingVisitor writer) {
        return new MappingDstNsReorder(writer, List.of("intermediary", "named")); // Remove hashed namespace
    }
}
