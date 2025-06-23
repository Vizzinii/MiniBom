package com.huawei.minibom.service;

import com.huawei.minibom.vo.ClassificationCreateFormVO;
import com.huawei.minibom.vo.ClassificationLinkAttrVO;
import com.huawei.minibom.vo.ClassificationUpdateFormVO;
import com.huawei.minibom.vo.CustomClassificationNodeViewVO;
import com.huawei.minibom.vo.ClassificationViewVO;

import java.util.List;

public interface IClassificationManagementService {

    String create(ClassificationCreateFormVO vo);

    void deleteById(String id);

    void update(ClassificationUpdateFormVO vo);

    List<CustomClassificationNodeViewVO> queryClassificationTree();

    List<CustomClassificationNodeViewVO> queryByNameOrCode(String name);

    void linkAttributes(ClassificationLinkAttrVO vo);

    ClassificationViewVO queryClassificationDetail(String id);
} 