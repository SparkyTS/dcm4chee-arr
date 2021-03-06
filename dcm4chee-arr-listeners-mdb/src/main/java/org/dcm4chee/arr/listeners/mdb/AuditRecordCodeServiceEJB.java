//
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.arr.listeners.mdb;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4chee.arr.entities.Code;

/**
 * Service for looking up and persisting ARR codes.
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@EJB(name = AuditRecordCodeService.JNDI_NAME, beanInterface = AuditRecordCodeService.class)
@Stateless
public class AuditRecordCodeServiceEJB implements AuditRecordCodeService {
    
    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;
    
    @Inject
    private AuditRecordCodeCache codeCache;
    
    @Override
    public void clearCache() {
        codeCache.clear();
    }
    
    @Override
    public Code findOrCreateCode(String codeValue, String codeDesignator, String codeMeaning) {
        Code code = codeCache.getCode(codeValue, codeDesignator);
        if(code != null) {
            return code;
        }
        
        @SuppressWarnings("unchecked")
        List<Code> codes = em.createQuery(
                        "SELECT c " +
                        "FROM org.dcm4chee.arr.entities.Code c " +
                        "WHERE c.value = :value " +
                        "AND c.designator = :designator")
                .setParameter("value", codeValue)
                .setParameter("designator", codeDesignator)
                .setHint("org.hibernate.readOnly", Boolean.TRUE)
                .getResultList();
        if (!codes.isEmpty()) {
            code = codes.get(0);
        } else {
            code = new Code();
            code.setValue(codeValue);
            code.setDesignator(codeDesignator);
            code.setMeaning(codeMeaning);
            try {
                em.persist(code);
            } catch (EJBException ex) {
                if (ex.getCause() instanceof EntityExistsException) {
                    // A Code with the same values must be inserted by a concurrent
                    // operation
                    // so we just retrieve it
                    code = findOrCreateCode(codeValue, codeDesignator, codeMeaning);
                } else {
                    throw ex;
                }
            }
        }
        
        codeCache.cacheCode(code);
        return code;
    }
    
}
