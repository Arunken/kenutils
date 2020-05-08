package com.ken.kenutils.fileUtils;

import java.io.IOException;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.DeobfuscationUtilities;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;

public class DecompileUtils {

	public static String decompile(String source) throws IOException{
			
		final DecompilerSettings settings = DecompilerSettings.javaDefaults();
		PlainTextOutput pText = new PlainTextOutput();
    	Decompiler.decompile(source, pText, settings);
	    return pText.toString();	
	}
	
	public static String decompileTest(String source) throws IOException{
		
		
		PlainTextOutput pText = new PlainTextOutput();
    	decompileData(source, pText);
	    return pText.toString();	
	}
	
	private static void decompileData(final String internalName, final ITextOutput output){
		final DecompilerSettings settings = DecompilerSettings.javaDefaults();
        VerifyArgument.notNull(internalName, "internalName");
        VerifyArgument.notNull(settings, "settings");

        final ITypeLoader typeLoader = settings.getTypeLoader() != null ? settings.getTypeLoader() : new InputTypeLoader();//pass typeloader in settings
        final MetadataSystem metadataSystem = new MetadataSystem(typeLoader);

        final TypeReference type;

        if (internalName.length() == 1) {
            //
            // Hack to get around classes whose descriptors clash with primitive types.
            //

            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(internalName);

            type = metadataSystem.resolve(reference);
        }
        else {
            type = metadataSystem.lookupType(internalName);
        }

        final TypeDefinition resolvedType;

        if (type == null || (resolvedType = type.resolve()) == null) {
            output.writeLine("!!! ERROR: Failed to load class %s.", internalName);
            return;
        }

        DeobfuscationUtilities.processType(resolvedType);

        final DecompilationOptions options = new DecompilationOptions();

        options.setSettings(settings);
        options.setFullDecompilation(true);

        if (settings.getJavaFormattingOptions() == null) {
            settings.setJavaFormattingOptions(JavaFormattingOptions.createDefault());
        }

        settings.getLanguage().decompileType(resolvedType, output, options);
    
	}
	
}
