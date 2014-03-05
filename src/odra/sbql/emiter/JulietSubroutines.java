package odra.sbql.emiter;


import odra.db.Database;
import odra.db.DatabaseException;
import odra.db.schema.OdraViewSchema;
import odra.sbql.interpreter.InterpreterException;


public class JulietSubroutines {
    //TODO move it to the database as regular procedures
    
   private static byte[] derefStructCode;

   private static byte[] derefVirtualCode;

   private static byte[] updateVirtualCode;

   private static byte[] deleteVirtualCode;

   private static byte[] createVirtualCode;

   private static byte[] navigateVirtualCode;
   // private byte[] derefBinderCode;

   static {
      derefStructCode = genDerefStructSubroutine().getByteCode();
      derefVirtualCode = genDerefVirtualSubroutine().getByteCode();
      updateVirtualCode = genUpdateVirtualSubroutine().getByteCode();
      deleteVirtualCode = genDeleteVirtualSubroutine().getByteCode();
      createVirtualCode = genCreateVirtualSubroutine().getByteCode();
      navigateVirtualCode = genNavigateVirtualSubroutine().getByteCode();
   }

   public final byte[] getDerefStructSubroutine() {
      return derefStructCode;
   }

   public final byte[] getDerefBinderSubroutine(int binderNameId) {

      byte[] derefBinderCode = genDerefBinderSubroutine(binderNameId).getByteCode();

      return derefBinderCode;
   }

   public final byte[] getDerefVirtualSubroutine() {
      return derefVirtualCode;
   }

   public final byte[] getUpdateVirtualSubroutine() {
      return updateVirtualCode;
   }

   public final byte[] getDeleteVirtualSubroutine() {
      return deleteVirtualCode;
   }

   public final byte[] getCreateVirtualSubroutine() {
      return createVirtualCode;
   }

   public final byte[] getNavigateVirtualSubroutine() {
      return navigateVirtualCode;
   }

   private final static JulietCode genDerefStructSubroutine() {
      JulietCode resJt = new JulietCode();

      JulietCode inLoop = new JulietCode();
      inLoop.emit(OpCodes.dynDeref);
      inLoop.emit(OpCodes.insPrt2);

      resJt.emit(OpCodes.tobag);
      resJt.emit(OpCodes.ldBag);
      resJt.emit(OpCodes.swap);
      resJt.append(JulietGen.genIterationWithCounter(inLoop));
      resJt.emit(OpCodes.fltn);
      resJt.emit(OpCodes.tostruct);
      resJt.emit(OpCodes.retSub);
      return resJt;
   }

   private final JulietCode genDerefBinderSubroutine(int binderName) {
      JulietCode resJt = new JulietCode();
      JulietCode inLoop = new JulietCode();

      inLoop.emit(OpCodes.dynDeref);
      inLoop.emit(OpCodes.insPrt2);

      resJt.emit(OpCodes.ldBag);
      resJt.emit(OpCodes.swap);
      resJt.append(JulietGen.genIterationWithCounter(inLoop));
      resJt.emit(OpCodes.fltn);
      resJt.emit(OpCodes.grAs, binderName);
      resJt.emit(OpCodes.retSub);
      return resJt;
   }

   /**
    * Generates Juliet code responsible for evaluation of view "on_retrieve"
    * 
    * @return Juliet code responsible for evaluation of view "on_retrieve"
    * @throws JulietCodeGeneratorException
    */
   private final static JulietCode genDerefVirtualSubroutine() {
      // assume that on top of the stack we have virtual identifier
      try {
         return prepareVirtualCall(
                  new JulietCode().emit(OpCodes.ldvGenPrc, Database.getStore().addName(
                           OdraViewSchema.GenericNames.ON_RETRIEVE_NAME.toString())), 0).emit(OpCodes.retSub);

      } catch (DatabaseException e) {
         throw new InterpreterException( e);
      }

   }

   /**
    * Generates Juliet code responsible for evaluation of view "on_update"
    * 
    * @return Juliet code responsible for evaluation of view "on_update"
    * @throws JulietCodeGeneratorException
    */
   private final static JulietCode genUpdateVirtualSubroutine() {
      // assume that on top of the stack we have virtual identifier
      try {
         return prepareVirtualCall(
                  new JulietCode().emit(OpCodes.ldvGenPrc, Database.getStore().addName(
                           OdraViewSchema.GenericNames.ON_UPDATE_NAME.toString())), 1).emit(OpCodes.pop).emit(OpCodes.retSub);

      } catch (DatabaseException e) {
         throw new InterpreterException(e);
      }

   }

   /**
    * Generates Juliet code responsible for evaluation of view "on_new"
    * 
    * @return Juliet code responsible for evaluation of view "on_new"
    * @throws JulietCodeGeneratorException
    */
   private final static JulietCode genCreateVirtualSubroutine() {
      // QRES top
      // reference to view definition
      // -----
      // param (it can be the collection)

      JulietCode inLoopJt = new JulietCode();

      JulietCode resJt = new JulietCode();
      resJt.emit(OpCodes.dup); // duplicate view reference
      resJt.emit(OpCodes.bswap2); // param - viewref - viewref

      try {
         // QRES singleparam - param - viewref - vieref
         // inLoopJt.emit(OpCodes.dynDeref);
         inLoopJt.emit(OpCodes.bswap2); // viewref - singleparam - param -
                                          // viewref
         inLoopJt.emit(OpCodes.ldI, 1); // on_new has one param
         inLoopJt.emit(OpCodes.swap);

         inLoopJt.emit(OpCodes.ldvGenPrc, Database.getStore().addName(OdraViewSchema.GenericNames.ON_NEW_NAME.toString()));
         inLoopJt.append(JulietGen.genProcedureCall());
         inLoopJt.emit(OpCodes.pop); // pop an empty bag -> param - viewref
         inLoopJt.emit(OpCodes.swap); // viewref - param
         inLoopJt.emit(OpCodes.dup); // duplicate view reference
         inLoopJt.emit(OpCodes.bswap2); // param - viewref - viewref
         resJt.append(JulietGen.genIterationWithCounter(inLoopJt));
         resJt.emit(OpCodes.pop);
         resJt.emit(OpCodes.pop);
         resJt.emit(OpCodes.ldBag);
         return resJt.emit(OpCodes.retSub); // subroutine return

      } catch (DatabaseException e) {
         throw new InterpreterException(e);
      }

   }

   /**
    * Generates Juliet code responsible for evaluation of view "on_delete"
    * 
    * @return Juliet code responsible for evaluation of view "on_delete"
    * @throws JulietCodeGeneratorException
    */
   private final static JulietCode genDeleteVirtualSubroutine() {
      // assume that on top of the stack we have virtual identifier

      try {
         return prepareVirtualCall(
                  new JulietCode().emit(OpCodes.ldvGenPrc, Database.getStore().addName(
                           OdraViewSchema.GenericNames.ON_DELETE_NAME.toString())), 0).emit(OpCodes.pop).emit(OpCodes.retSub);

      } catch (DatabaseException e) {
         throw new InterpreterException(e);
      }
   }

   /**
    * Generates Juliet code responsible for evaluation of view "on_delete"
    * 
    * @return Juliet code responsible for evaluation of view "on_delete"
    * @throws JulietCodeGeneratorException
    */
   private final static JulietCode genNavigateVirtualSubroutine() {
      // assume that on top of the stack we allready have reference to
      // on_navigate procedure
      JulietCode resJt = new JulietCode();

      resJt.emit(OpCodes.ldI, 0);
      resJt.emit(OpCodes.swap);
      resJt.append(JulietGen.genProcedureCall());
      resJt.emit(OpCodes.enterRefAsBinder);

      return resJt.emit(OpCodes.retSub); // subroutine return
   }

   private final static JulietCode prepareVirtualCall(JulietCode callCode, int paramsNo) {
      // assume that on top of the stack we have virtual identifier
      JulietCode resJt = new JulietCode();

      // resJt.append(JulietGen.createNestedEnvironment());
      resJt.append(callCode);
      resJt.emit(OpCodes.ldI, paramsNo);
      resJt.emit(OpCodes.swap);
      resJt.append(JulietGen.genProcedureCall());

      // resJt.append(JulietGen.destroyNestedEnvironment());

      return resJt; // subroutine return

   }
}