package org.smartregister.chw.model;

import org.apache.commons.lang3.ArrayUtils;
import org.smartregister.chw.util.ChildDBConstants;
import org.smartregister.family.model.BaseFamilyProfileMemberModel;

public class FamilyProfileMemberModel extends BaseFamilyProfileMemberModel {

    @Override
    protected String[] mainColumns(String tableName) {
        String[] columns = super.mainColumns(tableName);
        String[] newColumns = new String[]{
                tableName + "." + ChildDBConstants.KEY.ENTITY_TYPE
        };

        return ArrayUtils.addAll(columns, newColumns);
    }
}