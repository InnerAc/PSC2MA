/*************************************************************************
 * Copyright (c) 2006, 2008. All rights reserved. This program and the   
 * accompanying materials are made available under the terms of the      
 * Eclipse Public License v1.0 which accompanies this distribution,       
 * and is available at http://www.eclipse.org/legal/epl-v10.html         
 * 
 * Contributors:                                                         
 * Author: Su Zhiyong & Zhang Pengcheng                                 
 * Group: CSTV (Chair of Software Testing & Verification) Group          
 * E-mail: zhiyongsu@gmail.com, pchzhang@seu.edu.cn                     
 ***********************************************************************/

/***********************************************************************
 * Project: cn.cstv.wspscm                                          
 * Package: cn.cstv.wspscm.parts                                            
 * File: PartsFactory.java                                                   
 * Program: PartsFactory                                                
 * Version: J2SE-1.6.0                                                  
 * Date: 2008-7-21                                                        
 ***********************************************************************/

/**
 * 
 */
package cn.cstv.wspscm.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import cn.cstv.wspscm.model.Diagram;
import cn.cstv.wspscm.model.EnvironmentLifeline;
import cn.cstv.wspscm.model.EnvironmentLineConnection;
import cn.cstv.wspscm.model.Lifeline;
import cn.cstv.wspscm.model.LineConnection;
import cn.cstv.wspscm.model.Operator;
import cn.cstv.wspscm.model.PresentConstraint;

/**
 * @author Su Zhiyong & Zhang Pengcheng
 * 
 */
public class PartsFactory implements EditPartFactory {						//�����ཫ�ڴ���ģ�͵�ʱ�򱻵���

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart,
	 * java.lang.Object)
	 */
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		// TODO Auto-generated method stub
		EditPart editPart = null;
																			//���ݲ�ͬ��model�ֱ��������Ե�EditPartҲ���ǿ�����������model��EditPart����ϵ������
		if (model instanceof EnvironmentLifeline) {
			editPart = new EnvironmentLifelineEditPart();
		} else if (model instanceof Diagram) {
			editPart = new DiagramEditPart();
		} else if (model instanceof EnvironmentLineConnection) {
			editPart = new EnvironmentLineConnectionEditPart();
		}else if (model instanceof LineConnection) {
			editPart = new LineConnectionEditPart();
		} else if (model instanceof Operator) {
			editPart = new OperatorEditPart();
		}else if (model instanceof PresentConstraint){
			editPart = new PresentConstraintEditPart();
		}else if (model instanceof Lifeline){
			editPart = new LifelineEditPart();
		}
		
		if (editPart != null) {
			editPart.setModel(model);						//ģ���趨��������model��editpart֮��Ĺ�ϵ
		}
		return editPart;
	}

}
